package com.audine.dedalo.core.di.modules

import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.auth.data.UserDao
import com.audine.dedalo.chat.data.ChatMessageDao
import com.audine.dedalo.chat.data.ChatRepository
import com.audine.dedalo.chat.data.ChatRepositoryImpl
import com.audine.dedalo.chat.data.GeminiApiService
import com.audine.dedalo.core.data.local.TestData
import com.audine.dedalo.core.di.qualifier.ApplicationScope
import com.audine.dedalo.profile.data.GalleryDao
import com.audine.dedalo.profile.data.ProfileRepository
import com.audine.dedalo.projects.data.FirebaseSyncManager
import com.audine.dedalo.projects.data.ProjectDao
import com.audine.dedalo.projects.data.ProjectRepository
import com.audine.dedalo.projects.data.StageDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides @Singleton
    fun provideFirebaseSyncManager(
        firestore: FirebaseFirestore,
        projectDao: ProjectDao,
        stageDao: StageDao,
        @ApplicationScope scope: CoroutineScope
    ): FirebaseSyncManager {
        val manager = FirebaseSyncManager(firestore, projectDao, stageDao, scope)
        manager.startListening()
        return manager
    }

    @Provides @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        userDao: UserDao
    ): AuthRepository = AuthRepository(auth, userDao)

    @Provides @Singleton
    fun provideProjectRepository(
        projectDao: ProjectDao,
        stageDao: StageDao,
        firestore: FirebaseFirestore
    ): ProjectRepository = ProjectRepository(projectDao, stageDao, firestore)

    @Provides @Singleton
    fun provideChatRepository(
        dao: ChatMessageDao,
        api: GeminiApiService,
        apiKey: String
    ): ChatRepository = ChatRepositoryImpl(dao, api, apiKey)

    @Provides @Singleton
    fun provideProfileRepository(
        galleryDao: GalleryDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ProfileRepository = ProfileRepository(galleryDao, firestore, storage)

    @Provides @Singleton
    fun initTestData(
        projectDao: ProjectDao,
        stageDao: StageDao,
        @ApplicationScope scope: CoroutineScope
    ): TestData {
        scope.launch { TestData.seed(projectDao, stageDao) }
        return TestData
    }
}
