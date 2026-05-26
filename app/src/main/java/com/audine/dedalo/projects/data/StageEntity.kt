package com.audine.dedalo.projects.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stages",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["obraId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("obraId")]
)
data class StageEntity(
    @PrimaryKey val id: String = "",
    val obraId: String,
    val nombre: String,
    val posicion: Int,
    val estado: String,
    val fechaInicio: Long? = null,
    val fechaFin: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
