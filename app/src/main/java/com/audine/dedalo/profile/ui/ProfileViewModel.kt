package com.audine.dedalo.profile.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.profile.data.ProfileRepository
import com.audine.dedalo.profile.domain.ProfileUiState
import com.audine.dedalo.projects.data.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
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

    private fun lastSuccessGallery(): List<com.audine.dedalo.profile.data.GalleryImageEntity> =
        (_uiState.value as? ProfileUiState.Success)?.galleryImages
            ?: (_uiState.value as? ProfileUiState.UploadError)?.galleryImages
            ?: emptyList()

    fun uploadGalleryImage(uri: Uri) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            try {
                profileRepository.uploadGalleryImage(user.id, uri)
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
                val downloadUrl = profileRepository.uploadAvatar(user.id, uri)
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
