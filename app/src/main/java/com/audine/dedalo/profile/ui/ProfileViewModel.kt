package com.audine.dedalo.profile.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.profile.data.ProfileRepository
import com.audine.dedalo.profile.domain.ProfileUiState
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
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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

    fun uploadGalleryImage(uri: Uri) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            profileRepository.uploadGalleryImage(user.id, uri)
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            profileRepository.uploadAvatar(user.id, uri)
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
