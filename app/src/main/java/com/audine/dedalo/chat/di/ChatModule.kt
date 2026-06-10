package com.audine.dedalo.chat.di

import com.audine.dedalo.chat.data.ChatMessageDao
import com.audine.dedalo.chat.data.ChatRepository
import com.audine.dedalo.chat.data.ChatRepositoryImpl
import com.audine.dedalo.chat.data.GeminiApiService

object ChatModule {
    fun provideChatRepository(
        dao: ChatMessageDao,
        api: GeminiApiService,
        apiKey: String
    ): ChatRepository = ChatRepositoryImpl(dao, api, apiKey)
}
