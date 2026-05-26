package com.audine.dedalo.projects.data

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseSyncManager(
    private val firestore: FirebaseFirestore,
    private val projectDao: ProjectDao,
    private val stageDao: StageDao,
    private val scope: CoroutineScope
) {
    private var projectRegistration: ListenerRegistration? = null
    private var stagesRegistration: ListenerRegistration? = null

    fun startListening() {
        stopListening()
        projectRegistration = firestore.collection("projects")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    for (dc in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                val entity = dc.document.toObject(ProjectEntity::class.java)
                                    .copy(id = dc.document.id)
                                projectDao.upsert(entity)
                            }
                            DocumentChange.Type.REMOVED -> {
                                projectDao.deleteById(dc.document.id)
                            }
                        }
                    }
                }
            }

        stagesRegistration = firestore.collectionGroup("stages")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    for (dc in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                val entity = dc.document.toObject(StageEntity::class.java)
                                    .copy(id = dc.document.id)
                                stageDao.upsertAll(listOf(entity))
                            }
                            DocumentChange.Type.REMOVED -> {}
                        }
                    }
                }
            }
    }

    fun stopListening() {
        projectRegistration?.remove()
        projectRegistration = null
        stagesRegistration?.remove()
        stagesRegistration = null
    }
}
