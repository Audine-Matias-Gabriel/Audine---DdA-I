package com.audine.dedalo.projects.data

import androidx.annotation.Keep

@Keep
data class ImageData(
    val url: String = "",
    val type: ImageType = ImageType.PHOTO
)
