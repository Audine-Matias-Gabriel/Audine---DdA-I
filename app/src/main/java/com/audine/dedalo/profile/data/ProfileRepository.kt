package com.audine.dedalo.profile.data

import android.net.Uri
import com.audine.dedalo.core.data.remote.SupabaseStorageHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ProfileRepository(
    private val galleryDao: GalleryDao,
    private val firestore: FirebaseFirestore,
    private val supabaseStorageHelper: SupabaseStorageHelper
) {
    fun observeGallery(userId: String): Flow<List<GalleryImageEntity>> =
        galleryDao.observeAll(userId)

    suspend fun uploadGalleryImage(userId: String, uri: Uri): String {
        val imageId = UUID.randomUUID().toString()
        val path = "users/$userId/gallery/$imageId.jpg"
        val downloadUrl = supabaseStorageHelper.uploadImage(uri, path)

        val entity = GalleryImageEntity(
            id = imageId,
            userId = userId,
            imageUrl = downloadUrl
        )
        galleryDao.insert(entity)

        firestore.collection("users").document(userId)
            .collection("gallery").document(imageId)
            .set(mapOf("imageUrl" to downloadUrl, "createdAt" to entity.createdAt))

        return downloadUrl
    }

    suspend fun uploadAvatar(userId: String, uri: Uri): String {
        val path = "users/$userId/avatar.jpg"
        return supabaseStorageHelper.uploadImage(uri, path)
    }
}
