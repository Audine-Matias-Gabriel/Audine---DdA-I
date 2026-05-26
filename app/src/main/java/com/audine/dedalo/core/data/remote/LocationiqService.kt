package com.audine.dedalo.core.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationiqService {

    @GET("v1/autocomplete")
    suspend fun autocomplete(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): List<LocationiqResponse>

    companion object {
        fun create(): LocationiqService {
            return Retrofit.Builder()
                .baseUrl("https://api.locationiq.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LocationiqService::class.java)
        }
    }
}
