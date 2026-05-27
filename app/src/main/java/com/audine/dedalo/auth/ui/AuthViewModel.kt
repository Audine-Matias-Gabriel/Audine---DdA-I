package com.audine.dedalo.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.auth.domain.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { authState ->
                _uiState.update {
                    when (authState) {
                        is AuthRepository.AuthState.Loading -> AuthUiState.Loading
                        is AuthRepository.AuthState.Unauthenticated -> AuthUiState.Unauthenticated
                        is AuthRepository.AuthState.Authenticated -> AuthUiState.Authenticated(authState.user)
                        is AuthRepository.AuthState.Error -> AuthUiState.Error(authState.message)
                    }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            authRepository.clearUser()
        }
    }
}
