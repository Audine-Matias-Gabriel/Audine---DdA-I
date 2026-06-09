package com.audine.dedalo.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.audine.dedalo.auth.data.UserDao
import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.chat.data.ChatMessageDao
import com.audine.dedalo.chat.data.ChatMessageEntity
import com.audine.dedalo.projects.data.ProjectDao
import com.audine.dedalo.projects.data.ProjectEntity
import com.audine.dedalo.projects.data.StageDao
import com.audine.dedalo.projects.data.StageEntity

@Database(
    entities = [ProjectEntity::class, StageEntity::class, UserEntity::class, ChatMessageEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DedaloDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun stageDao(): StageDao
    abstract fun userDao(): UserDao
    abstract fun chatMessageDao(): ChatMessageDao
}
