package com.audine.dedalo.core.di.modules

import android.content.Context
import androidx.room.Room
import com.audine.dedalo.auth.data.UserDao
import com.audine.dedalo.chat.data.ChatMessageDao
import com.audine.dedalo.core.data.local.DedaloDatabase
import com.audine.dedalo.profile.data.GalleryDao
import com.audine.dedalo.projects.data.ProjectDao
import com.audine.dedalo.projects.data.StageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DedaloDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            DedaloDatabase::class.java,
            "dedalo.db"
        ).fallbackToDestructiveMigration().build()

    @Provides @Singleton
    fun provideProjectDao(db: DedaloDatabase): ProjectDao = db.projectDao()

    @Provides @Singleton
    fun provideStageDao(db: DedaloDatabase): StageDao = db.stageDao()

    @Provides @Singleton
    fun provideUserDao(db: DedaloDatabase): UserDao = db.userDao()

    @Provides @Singleton
    fun provideChatMessageDao(db: DedaloDatabase): ChatMessageDao = db.chatMessageDao()

    @Provides @Singleton
    fun provideGalleryDao(db: DedaloDatabase): GalleryDao = db.galleryDao()
}
