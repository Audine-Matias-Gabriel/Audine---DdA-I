package com.audine.dedalo.chat.data

import com.audine.dedalo.chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val dao: ChatMessageDao,
    private val api: GeminiApiService,
    private val apiKey: String
) : ChatRepository {

    override val messages: Flow<List<ChatMessage>> = dao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun sendMessage(text: String) {
        if (apiKey.isBlank()) {
            throw IllegalStateException(
                "Gemini API key no configurada. Agregala en secrets.properties"
            )
        }

        dao.insert(ChatMessageEntity(role = "user", content = text))

        val history = dao.getAll()
        val geminiHistory = history.map { GeminiContent(it.role, listOf(GeminiPart(it.content))) }
        val response = api.generateContent(apiKey, GeminiRequest(geminiHistory))
        val reply = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: "No se pudo obtener respuesta"

        dao.insert(ChatMessageEntity(role = "model", content = reply))
    }

    override suspend fun clearHistory() {
        dao.deleteAll()
    }
}
