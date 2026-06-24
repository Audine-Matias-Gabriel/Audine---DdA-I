package com.audine.dedalo.projects.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseSyncManager(
    private val firestore: FirebaseFirestore,
    private val projectDao: ProjectDao,
    private val stageDao: StageDao,
    private val auth: FirebaseAuth,
    private val scope: CoroutineScope
) {
    private val gson = Gson()
    private val imageDataType = object : TypeToken<List<ImageData>>() {}.type

    private var projectRegistration: ListenerRegistration? = null
    private var stagesRegistration: ListenerRegistration? = null
    private var authRegistration: FirebaseAuth.AuthStateListener? = null

    fun startListening() {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            restartListeners(userId)
        }
        authRegistration = listener
        auth.addAuthStateListener(listener)
    }

    private fun parseImages(document: com.google.firebase.firestore.DocumentSnapshot): List<ImageData> {
        return try {
            val raw = document.get("images")
            if (raw is List<*>) {
                val json = gson.toJson(raw)
                gson.fromJson(json, imageDataType) ?: emptyList()
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun restartListeners(userId: String?) {
        stopListeners()
        val uid = userId ?: "__none__"

        projectRegistration = firestore.collection("projects")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    for (dc in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                val entity = dc.document.toObject(ProjectEntity::class.java)
                                    .copy(id = dc.document.id, images = parseImages(dc.document))
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
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    for (dc in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                val entity = dc.document.toObject(StageEntity::class.java)
                                    .copy(id = dc.document.id, images = parseImages(dc.document))
                                stageDao.upsertAll(listOf(entity))
                            }
                            DocumentChange.Type.REMOVED -> {}
                        }
                    }
                }
            }
    }

    fun stopListening() {
        authRegistration?.let { auth.removeAuthStateListener(it) }
        authRegistration = null
        stopListeners()
    }

    private fun stopListeners() {
        projectRegistration?.remove()
        projectRegistration = null
        stagesRegistration?.remove()
        stagesRegistration = null
    }
}
