package com.audine.dedalo.chat.data

import com.audine.dedalo.chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    val messages: Flow<List<ChatMessage>>
    suspend fun sendMessage(text: String)
    suspend fun clearHistory()
}
