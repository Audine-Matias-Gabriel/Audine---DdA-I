package com.audine.dedalo.projects.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StageDao {

    @Query("SELECT * FROM stages WHERE obraId = :obraId ORDER BY posicion ASC")
    fun observeByObraId(obraId: String): Flow<List<StageEntity>>

    @Query("SELECT * FROM stages WHERE obraId = :obraId ORDER BY posicion ASC")
    suspend fun getByObraId(obraId: String): List<StageEntity>

    @Upsert
    suspend fun upsertAll(stages: List<StageEntity>)

    @Query("DELETE FROM stages WHERE obraId = :obraId")
    suspend fun deleteByObraId(obraId: String)

    @Query("DELETE FROM stages")
    suspend fun deleteAll()
}
