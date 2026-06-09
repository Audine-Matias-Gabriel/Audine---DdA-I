package com.audine.dedalo.chat.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain() = com.audine.dedalo.chat.domain.model.ChatMessage(
        id = id,
        role = role,
        content = content,
        timestamp = timestamp
    )
}
