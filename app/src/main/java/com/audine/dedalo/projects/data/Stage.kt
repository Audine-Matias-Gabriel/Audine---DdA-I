package com.audine.dedalo.projects.data

enum class StageStatus { ESPERA, EN_PROGRESO, FINALIZADA }

data class Stage(
    val id: String = "",
    val obraId: String = "",
    val nombre: String,
    val posicion: Int,
    val estado: StageStatus = StageStatus.ESPERA,
    val fechaInicio: Long? = null,
    val fechaFin: Long? = null,
    val images: List<ImageData> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
