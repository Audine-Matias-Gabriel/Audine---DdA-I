package com.audine.dedalo.profile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {

    @Query("SELECT * FROM gallery_images WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeAll(userId: String): Flow<List<GalleryImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: GalleryImageEntity)

    @Query("DELETE FROM gallery_images WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM gallery_images WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}
