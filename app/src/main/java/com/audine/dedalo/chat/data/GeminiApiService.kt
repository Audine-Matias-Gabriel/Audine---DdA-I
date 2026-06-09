package com.audine.dedalo.chat.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>
)

data class GeminiPart(val text: String)

data class GeminiRequest(val contents: List<GeminiContent>)

data class GeminiResponse(val candidates: List<GeminiCandidate>?)

data class GeminiCandidate(val content: GeminiContent?)

interface GeminiApiService {

    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    companion object {
        fun create(): GeminiApiService {
            return Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeminiApiService::class.java)
        }
    }
}
