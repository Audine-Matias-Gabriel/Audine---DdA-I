package com.audine.dedalo.projects.ui.detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.projects.data.ImageData
import com.audine.dedalo.projects.data.Project
import com.audine.dedalo.projects.data.ProjectRepository
import com.audine.dedalo.projects.data.Stage
import com.audine.dedalo.projects.data.StageStatus
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: ProjectRepository = mockk()
    private val storage: FirebaseStorage = mockk()
    private val authRepository: AuthRepository = mockk()
    private val savedStateHandle = SavedStateHandle(mapOf("obraId" to "test-123"))

    private val stage = Stage(
        id = "stage-1", nombre = "Excavación", posicion = 1,
        estado = StageStatus.ESPERA
    )

    private val project = Project(
        id = "test-123",
        nombre = "Test Project",
        direccion = "Test Address",
        stages = listOf(stage)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        every { repository.observeProjectWithStages(any()) } returns flowOf(null)

        val viewModel = ProjectDetailViewModel(repository, storage, authRepository, savedStateHandle)

        assert(viewModel.uiState.value is ProjectDetailUiState.Loading)
    }

    @Test
    fun `when project emits state becomes Success`() = runTest(testDispatcher) {
        every { repository.observeProjectWithStages(any()) } returns flowOf(project)

        val viewModel = ProjectDetailViewModel(repository, storage, authRepository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ProjectDetailUiState.Success)
            assert((state as ProjectDetailUiState.Success).project == project)
        }
    }

    @Test
    fun `when project is null state becomes Error`() = runTest(testDispatcher) {
        every { repository.observeProjectWithStages(any()) } returns flowOf(null)

        val viewModel = ProjectDetailViewModel(repository, storage, authRepository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ProjectDetailUiState.Error)
            assert((state as ProjectDetailUiState.Error).message == "Obra no encontrada")
        }
    }

    @Test
    fun `addStage calls repository createStage and hides dialog`() = runTest(testDispatcher) {
        every { repository.observeProjectWithStages(any()) } returns flowOf(project)
        every { authRepository.currentUserId } returns "test-user"
        coEvery { repository.createStage(any(), any(), any()) } returns "new-stage-id"

        val viewModel = ProjectDetailViewModel(repository, storage, authRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.showAddStageDialog()
        assert(viewModel.showAddStageDialog.value)

        viewModel.addStage("Nueva Etapa")

        coVerify { repository.createStage("test-123", any(), "test-user") }
        assert(!viewModel.showAddStageDialog.value)
    }

    @Test
    fun `updateStageStatus calls repository updateStage`() = runTest(testDispatcher) {
        every { repository.observeProjectWithStages(any()) } returns flowOf(project)
        coEvery { repository.updateStage(any(), any()) } returns Unit

        val viewModel = ProjectDetailViewModel(repository, storage, authRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateStageStatus(stage, StageStatus.EN_PROGRESO)

        coVerify { repository.updateStage("test-123", stage.copy(estado = StageStatus.EN_PROGRESO)) }
        assert(viewModel.stageStatusDialog.value == null)
    }

    @Test
    fun `onImagePicked sets pendingImageUri`() = runTest(testDispatcher) {
        every { repository.observeProjectWithStages(any()) } returns flowOf(project)
        val uri = mockk<Uri>()

        val viewModel = ProjectDetailViewModel(repository, storage, authRepository, savedStateHandle)

        viewModel.onImagePicked(uri)

        assert(viewModel.pendingImageUri.value == uri)
    }

    @Test
    fun `cancelImageDestination clears pendingImageUri`() = runTest(testDispatcher) {
        every { repository.observeProjectWithStages(any()) } returns flowOf(project)
        val uri = mockk<Uri>()
        val storageRef = mockk<StorageReference>(relaxed = true)

        val viewModel = ProjectDetailViewModel(repository, storage, authRepository, savedStateHandle)

        viewModel.onImagePicked(uri)
        assert(viewModel.pendingImageUri.value == uri)

        viewModel.cancelImageDestination()
        assert(viewModel.pendingImageUri.value == null)
    }
}
