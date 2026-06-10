package com.audine.dedalo.profile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gallery_images")
data class GalleryImageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)
