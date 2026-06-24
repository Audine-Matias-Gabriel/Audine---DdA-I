package com.audine.dedalo.profile.domain

import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.profile.data.GalleryImageEntity

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val user: UserEntity,
        val galleryImages: List<GalleryImageEntity>
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data class UploadError(
        val message: String,
        val user: UserEntity,
        val galleryImages: List<GalleryImageEntity>
    ) : ProfileUiState
}
