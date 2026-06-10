package com.audine.dedalo.core.di.modules

import com.audine.dedalo.BuildConfig
import com.audine.dedalo.chat.data.GeminiApiService
import com.audine.dedalo.core.data.remote.LocationiqService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideLocationiqService(): LocationiqService = LocationiqService.create()

    @Provides @Singleton
    fun provideGeminiApiService(): GeminiApiService = GeminiApiService.create()

    @Provides @Singleton
    fun provideGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY
}
