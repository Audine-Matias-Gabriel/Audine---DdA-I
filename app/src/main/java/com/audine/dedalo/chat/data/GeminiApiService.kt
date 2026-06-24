package com.audine.dedalo.chat.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>
)

data class GeminiPart(val text: String)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerializedName("system_instruction")
    val systemInstruction: GeminiContent? = null
)

data class GeminiResponse(val candidates: List<GeminiCandidate>?)

data class GeminiCandidate(val content: GeminiContent?)

interface GeminiApiService {

    @POST("v1beta/models/gemini-2.5-flash-lite:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    companion object {
        fun create(): GeminiApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeminiApiService::class.java)
        }
    }
}
