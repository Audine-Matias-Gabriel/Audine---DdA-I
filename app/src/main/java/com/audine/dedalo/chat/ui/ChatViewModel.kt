package com.audine.dedalo.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.chat.data.ChatRepository
import com.audine.dedalo.chat.domain.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _isLoadingResponse = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

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
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            _error.value = null
            _isLoadingResponse.value = true
            try {
                chatRepository.sendMessage(trimmed)
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
