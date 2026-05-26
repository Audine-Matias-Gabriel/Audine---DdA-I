package com.audine.dedalo.auth.domain

import com.audine.dedalo.auth.data.UserEntity

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Authenticated(val user: UserEntity) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
