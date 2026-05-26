package com.audine.dedalo.projects.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val stageDao: StageDao,
    private val firestore: FirebaseFirestore
) {

    fun observeProjects(): Flow<List<Project>> =
        projectDao.observeAll().map { entities ->
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

    suspend fun createProject(project: Project): String {
        val docRef = firestore.collection("projects").document()
        val entity = project.toEntity().copy(id = docRef.id)
        docRef.set(entity).addOnFailureListener { throw it }
        return docRef.id
    }

    suspend fun updateProject(project: Project) {
        firestore.collection("projects").document(project.id)
            .set(project.toEntity().copy(updatedAt = System.currentTimeMillis()))
            .addOnFailureListener { throw it }
    }

    suspend fun deleteProject(obraId: String) {
        firestore.collection("projects").document(obraId)
            .delete()
            .addOnFailureListener { throw it }
    }

    suspend fun createStage(obraId: String, stage: Stage): String {
        val docRef = firestore.collection("projects")
            .document(obraId)
            .collection("stages")
            .document()
        val entity = stage.toEntity().copy(id = docRef.id, obraId = obraId)
        docRef.set(entity).addOnFailureListener { throw it }
        return docRef.id
    }

    suspend fun updateStage(obraId: String, stage: Stage) {
        firestore.collection("projects").document(obraId)
            .collection("stages").document(stage.id)
            .set(stage.toEntity().copy(obraId = obraId, updatedAt = System.currentTimeMillis()))
            .addOnFailureListener { throw it }
    }

    private fun ProjectEntity.toDomain(): Project = Project(
        id = id, nombre = nombre, direccion = direccion,
        latitud = latitud, longitud = longitud,
        fos = fos, fot = fot, imageUrls = imageUrls,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun StageEntity.toDomain(): Stage = Stage(
        id = id, obraId = obraId, nombre = nombre, posicion = posicion,
        estado = try { StageStatus.valueOf(estado) } catch (_: IllegalArgumentException) { StageStatus.ESPERA },
        fechaInicio = fechaInicio, fechaFin = fechaFin,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun Project.toEntity(): ProjectEntity = ProjectEntity(
        id = id, nombre = nombre, direccion = direccion,
        latitud = latitud, longitud = longitud,
        fos = fos, fot = fot, imageUrls = imageUrls,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun Stage.toEntity(): StageEntity = StageEntity(
        id = id, obraId = obraId, nombre = nombre, posicion = posicion,
        estado = estado.name,
        fechaInicio = fechaInicio, fechaFin = fechaFin,
        createdAt = createdAt, updatedAt = updatedAt
    )
}
