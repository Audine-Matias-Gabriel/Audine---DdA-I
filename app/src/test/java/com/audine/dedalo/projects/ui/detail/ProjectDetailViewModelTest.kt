package com.audine.dedalo.projects.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.audine.dedalo.projects.data.Project
import com.audine.dedalo.projects.data.ProjectRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
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
    private val savedStateHandle = SavedStateHandle(mapOf("obraId" to "test-123"))

    private val project = Project(
        id = "test-123",
        nombre = "Test Project",
        direccion = "Test Address"
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

        val viewModel = ProjectDetailViewModel(repository, savedStateHandle)

        assert(viewModel.uiState.value is ProjectDetailUiState.Loading)
    }

    @Test
    fun `when project emits state becomes Success`() = runTest(testDispatcher) {
        every { repository.observeProjectWithStages(any()) } returns flowOf(project)

        val viewModel = ProjectDetailViewModel(repository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ProjectDetailUiState.Success)
            assert((state as ProjectDetailUiState.Success).project == project)
        }
    }

    @Test
    fun `when project is null state becomes Error`() = runTest(testDispatcher) {
        every { repository.observeProjectWithStages(any()) } returns flowOf(null)

        val viewModel = ProjectDetailViewModel(repository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ProjectDetailUiState.Error)
            assert((state as ProjectDetailUiState.Error).message == "Obra no encontrada")
        }
    }
}
