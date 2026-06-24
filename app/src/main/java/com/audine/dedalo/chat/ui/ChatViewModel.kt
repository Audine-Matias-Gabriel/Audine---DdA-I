package com.audine.dedalo.chat.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.chat.data.ChatRepository
import com.audine.dedalo.chat.domain.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _isLoadingResponse = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isOnCooldown = MutableStateFlow(false)

    val uiState: StateFlow<ChatUiState> = combine(
        chatRepository.messages,
        _isLoadingResponse,
        _error
    ) { messages, loading, error ->
        if (error != null) ChatUiState.Error(error, messages)
        else ChatUiState.Success(messages, loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState.Loading)

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isLoadingResponse.value || _isOnCooldown.value) return

        _error.value = null
        _isLoadingResponse.value = true
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(trimmed)
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("ChatVM", "HTTP 429 - errorBody: $errorBody")
                    _isOnCooldown.value = true
                    launch {
                        delay(60_000)
                        _isOnCooldown.value = false
                    }
                }
                _error.value = if (e.code() == 429) {
                    "Demasiadas solicitudes. Esperá unos segundos y volvé a intentar."
                } else {
                    "Error del servidor: ${e.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión"
            } finally {
                _isLoadingResponse.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatRepository.clearHistory()
        }
    }

    fun clearError() {
        _error.value = null
    }
}
