package com.audine.dedalo.projects.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val stageDao: StageDao,
    private val firestore: FirebaseFirestore
) {
    private val gson = Gson()

    fun observeProjectsByUser(userId: String): Flow<List<Project>> =
        projectDao.observeByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    fun observeProjectWithStages(obraId: String): Flow<Project?> =
        projectDao.observeById(obraId).combine(
            stageDao.observeByObraId(obraId)
        ) { projectEntity, stageEntities ->
            projectEntity?.toDomain()?.copy(
                stages = stageEntities.map { it.toDomain() }
            )
        }

    private fun ProjectEntity.toFirestoreMap(): Map<String, Any?> {
        val imagesJson = gson.toJson(images)
        @Suppress("UNCHECKED_CAST")
        val rawMap = gson.fromJson(imagesJson, MutableList::class.java)
        return mapOf(
            "id" to id,
            "userId" to userId,
            "nombre" to nombre,
            "direccion" to direccion,
            "latitud" to latitud,
            "longitud" to longitud,
            "fos" to fos,
            "fot" to fot,
            "images" to rawMap,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    private fun StageEntity.toFirestoreMap(): Map<String, Any?> {
        val imagesJson = gson.toJson(images)
        @Suppress("UNCHECKED_CAST")
        val rawMap = gson.fromJson(imagesJson, MutableList::class.java)
        return mapOf(
            "id" to id,
            "userId" to userId,
            "obraId" to obraId,
            "nombre" to nombre,
            "posicion" to posicion,
            "estado" to estado,
            "fechaInicio" to fechaInicio,
            "fechaFin" to fechaFin,
            "images" to rawMap,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    suspend fun createProject(project: Project, userId: String): String {
        val docRef = firestore.collection("projects").document()
        val entity = project.toEntity().copy(id = docRef.id, userId = userId)
        docRef.set(entity.toFirestoreMap() as Any).await()
        projectDao.upsert(entity.copy(userId = userId))
        return docRef.id
    }

    suspend fun updateProject(project: Project) {
        val entity = project.toEntity().copy(updatedAt = System.currentTimeMillis())
        firestore.collection("projects").document(project.id)
            .set(entity.toFirestoreMap() as Any)
            .await()
        projectDao.upsert(entity)
    }

    suspend fun deleteProject(obraId: String) {
        firestore.collection("projects").document(obraId)
            .delete()
            .await()
        projectDao.deleteById(obraId)
        stageDao.deleteByObraId(obraId)
    }

    suspend fun createStage(obraId: String, stage: Stage, userId: String): String {
        val docRef = firestore.collection("projects")
            .document(obraId)
            .collection("stages")
            .document()
        val entity = stage.toEntity().copy(id = docRef.id, obraId = obraId, userId = userId)
        docRef.set(entity.toFirestoreMap() as Any).await()
        stageDao.upsertAll(listOf(entity))
        return docRef.id
    }

    suspend fun updateStage(obraId: String, stage: Stage) {
        val entity = stage.toEntity().copy(obraId = obraId, updatedAt = System.currentTimeMillis())
        firestore.collection("projects").document(obraId)
            .collection("stages").document(stage.id)
            .set(entity.toFirestoreMap() as Any)
            .await()
        stageDao.upsertAll(listOf(entity))
    }

    suspend fun syncMyProjectsToFirebase(userId: String) {
        val projects = projectDao.getByUserId(userId)
        val obraIds = projects.map { it.id }
        val stages = stageDao.getByObraIds(obraIds)

        val projectsToDelete = firestore.collection("projects")
            .whereEqualTo("userId", userId)
            .get().await()
            .documents.map { it.id }
            .toMutableList()

        projectsToDelete.removeAll(projects.map { it.id })
        for (id in projectsToDelete) {
            firestore.collection("projects").document(id).delete().await()
            stageDao.deleteByObraId(id)
        }

        val allWrites = mutableListOf<Pair<String, Map<String, Any?>>>()
        projects.forEach { allWrites.add("projects/${it.id}" to it.toFirestoreMap()) }
        stages.forEach { allWrites.add("projects/${it.obraId}/stages/${it.id}" to it.toFirestoreMap()) }

        allWrites.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { (path, data) ->
                batch.set(firestore.document(path), data as Any)
            }
            batch.commit().await()
        }
    }

    private fun ProjectEntity.toDomain(): Project = Project(
        id = id, userId = userId, nombre = nombre, direccion = direccion,
        latitud = latitud, longitud = longitud,
        fos = fos, fot = fot, images = images,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun StageEntity.toDomain(): Stage = Stage(
        id = id, obraId = obraId, nombre = nombre, posicion = posicion,
        estado = try { StageStatus.valueOf(estado) } catch (_: IllegalArgumentException) { StageStatus.ESPERA },
        fechaInicio = fechaInicio, fechaFin = fechaFin,
        images = images,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun Project.toEntity(): ProjectEntity = ProjectEntity(
        id = id, userId = userId, nombre = nombre, direccion = direccion,
        latitud = latitud, longitud = longitud,
        fos = fos, fot = fot, images = images,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun Stage.toEntity(): StageEntity = StageEntity(
        id = id, obraId = obraId, nombre = nombre, posicion = posicion,
        estado = estado.name, images = images,
        fechaInicio = fechaInicio, fechaFin = fechaFin,
        createdAt = createdAt, updatedAt = updatedAt
    )
}
