package com.audine.dedalo.projects.data

data class Project(
    val id: String = "",
    val userId: String = "",
    val nombre: String,
    val direccion: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fos: String = "",
    val fot: String = "",
    val images: List<ImageData> = emptyList(),
    val stages: List<Stage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
