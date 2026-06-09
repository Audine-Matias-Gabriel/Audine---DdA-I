package com.audine.dedalo.core.di

import android.content.Context
import androidx.room.Room
import com.audine.dedalo.BuildConfig
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.auth.data.UserDao
import com.audine.dedalo.chat.data.ChatRepository
import com.audine.dedalo.chat.data.GeminiApiService
import com.audine.dedalo.core.data.local.DedaloDatabase
import com.audine.dedalo.core.data.local.TestData
import com.audine.dedalo.core.data.remote.LocationiqService
import com.audine.dedalo.projects.data.FirebaseSyncManager
import com.audine.dedalo.projects.data.ProjectDao
import com.audine.dedalo.projects.data.ProjectRepository
import com.audine.dedalo.projects.data.StageDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val database: DedaloDatabase = Room.databaseBuilder(
        context.applicationContext,
        DedaloDatabase::class.java,
        "dedalo.db"
    ).fallbackToDestructiveMigration().build()

    val projectDao: ProjectDao = database.projectDao()
    val stageDao: StageDao = database.stageDao()
    val userDao: UserDao = database.userDao()

    val locationiqService: LocationiqService = LocationiqService.create()
    val geminiApiService: GeminiApiService = GeminiApiService.create()
    val chatRepository = ChatRepository(
        dao = database.chatMessageDao(),
        api = geminiApiService,
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val firebaseSyncManager = FirebaseSyncManager(
        firestore = firestore,
        projectDao = projectDao,
        stageDao = stageDao,
        scope = applicationScope
    )

    val authRepository = AuthRepository(
        auth = auth,
        userDao = userDao
    )

    val projectRepository = ProjectRepository(
        projectDao = projectDao,
        stageDao = stageDao,
        firestore = firestore
    )

    init {
        firebaseSyncManager.startListening()
        applicationScope.launch {
            TestData.seed(projectDao, stageDao)
        }
    }
}
