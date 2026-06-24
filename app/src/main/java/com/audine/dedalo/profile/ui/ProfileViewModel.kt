package com.audine.dedalo.profile.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.profile.data.ProfileRepository
import com.audine.dedalo.profile.domain.ProfileUiState
import com.audine.dedalo.projects.data.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private var currentUser: UserEntity? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.flatMapLatest { user ->
                currentUser = user
                if (user != null) {
                    profileRepository.observeGallery(user.id).map { gallery ->
                        ProfileUiState.Success(user = user, galleryImages = gallery)
                    }
                } else {
                    flowOf(ProfileUiState.Loading)
                }
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    private suspend fun resizeImage(uri: Uri, maxSize: Int = 1024): ByteArray = withContext(Dispatchers.IO) {
        val inputStream = application.contentResolver.openInputStream(uri)
            ?: return@withContext ByteArray(0)
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: run {
            inputStream.close(); return@withContext ByteArray(0)
        }
        inputStream.close()

        val scale = minOf(1f, maxSize.toFloat() / maxOf(bitmap.width, bitmap.height))
        val output = ByteArrayOutputStream()
        if (scale >= 1f) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
        } else {
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            resized.compress(Bitmap.CompressFormat.JPEG, 85, output)
            resized.recycle()
        }
        bitmap.recycle()
        output.toByteArray()
    }

    private fun lastSuccessGallery(): List<com.audine.dedalo.profile.data.GalleryImageEntity> =
        (_uiState.value as? ProfileUiState.Success)?.galleryImages
            ?: (_uiState.value as? ProfileUiState.UploadError)?.galleryImages
            ?: emptyList()

    fun uploadGalleryImage(uri: Uri) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            try {
                val bytes = resizeImage(uri)
                if (bytes.isEmpty()) throw Exception("No se pudo leer la imagen")
                profileRepository.uploadGalleryImage(user.id, bytes)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.UploadError(
                    message = e.message ?: "Error al subir imagen a la galería",
                    user = user,
                    galleryImages = lastSuccessGallery()
                )
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            try {
                val bytes = resizeImage(uri)
                if (bytes.isEmpty()) throw Exception("No se pudo leer la imagen")
                val downloadUrl = profileRepository.uploadAvatar(user.id, bytes)
                authRepository.updatePhotoUrl(user.id, downloadUrl)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.UploadError(
                    message = e.message ?: "Error al subir avatar",
                    user = user,
                    galleryImages = lastSuccessGallery()
                )
            }
        }
    }

    fun clearUploadError() {
        _uiState.update { current ->
            if (current is ProfileUiState.UploadError) {
                ProfileUiState.Success(user = current.user, galleryImages = current.galleryImages)
            } else current
        }
    }

    fun syncProjects() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            _isSyncing.value = true
            try {
                projectRepository.syncMyProjectsToFirebase(userId)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            authRepository.clearUser()
            onSignedOut()
        }
    }
}
