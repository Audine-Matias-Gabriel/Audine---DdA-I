package com.audine.dedalo.chat.domain

import com.audine.dedalo.chat.domain.model.ChatMessage

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data class Success(
        val messages: List<ChatMessage>,
        val isLoadingResponse: Boolean = false
    ) : ChatUiState
    data class Error(
        val message: String,
        val messages: List<ChatMessage> = emptyList()
    ) : ChatUiState
}
