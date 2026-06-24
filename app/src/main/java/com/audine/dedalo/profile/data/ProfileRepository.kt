package com.audine.dedalo.profile.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProfileRepository(
    private val galleryDao: GalleryDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    fun observeGallery(userId: String): Flow<List<GalleryImageEntity>> =
        galleryDao.observeAll(userId)

    suspend fun uploadGalleryImage(userId: String, imageBytes: ByteArray): String {
        val imageId = UUID.randomUUID().toString()
        val ref = storage.reference.child("users/$userId/gallery/$imageId.jpg")
        ref.putBytes(imageBytes).await()
        val downloadUrl = ref.downloadUrl.await().toString()

        val entity = GalleryImageEntity(
            id = imageId,
            userId = userId,
            imageUrl = downloadUrl
        )
        galleryDao.insert(entity)

        firestore.collection("users").document(userId)
            .collection("gallery").document(imageId)
            .set(mapOf("imageUrl" to downloadUrl, "createdAt" to entity.createdAt))
            .await()

        return downloadUrl
    }

    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): String {
        val ref = storage.reference.child("users/$userId/avatar.jpg")
        ref.putBytes(imageBytes).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun updateAvatarUrl(userId: String, downloadUrl: String) {
        firestore.collection("users").document(userId)
            .update("photoUrl", downloadUrl).await()
    }
}
