package com.audine.dedalo.core.data.remote

import com.google.gson.annotations.SerializedName

data class LocationiqResponse(
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String,
    @SerializedName("display_name") val displayName: String
)
