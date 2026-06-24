package com.audine.dedalo.projects.ui.list

import app.cash.turbine.test
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.projects.data.Project
import com.audine.dedalo.projects.data.ProjectRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: ProjectRepository = mockk()
    private val authRepository: AuthRepository = mockk()

    private val testUser = UserEntity(
        id = "test-user",
        email = "test@test.com",
        displayName = "Test User"
    )

    private val projects = listOf(
        Project(id = "1", userId = "test-user", nombre = "Torres del Parque", direccion = "Av. Del Libertador 4950"),
        Project(id = "2", userId = "test-user", nombre = "Edificio Corporativo", direccion = "Av. Cabildo 3000"),
        Project(id = "3", userId = "test-user", nombre = "Hospital Municipal", direccion = "Av. Sáenz 1500")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.currentUser } returns flowOf(testUser)
        every { repository.observeProjectsByUser("test-user") } returns flowOf(projects)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        val viewModel = ProjectsViewModel(repository, authRepository)

        assert(viewModel.uiState.value is ProjectsUiState.Loading)
    }

    @Test
    fun `when projects emit state becomes Success`() = runTest(testDispatcher) {
        val viewModel = ProjectsViewModel(repository, authRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ProjectsUiState.Success)
            assert((state as ProjectsUiState.Success).projects == projects)
        }
    }

    @Test
    fun `search query filters projects by name`() = runTest(testDispatcher) {
        val viewModel = ProjectsViewModel(repository, authRepository)

        viewModel.onSearchQueryChange("torres")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ProjectsUiState.Success)
            val filtered = (state as ProjectsUiState.Success).projects
            assert(filtered.size == 1)
            assert(filtered[0].nombre == "Torres del Parque")
        }
    }

    @Test
    fun `search query filters projects by address`() = runTest(testDispatcher) {
        val viewModel = ProjectsViewModel(repository, authRepository)

        viewModel.onSearchQueryChange("Sáenz")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ProjectsUiState.Success)
            val filtered = (state as ProjectsUiState.Success).projects
            assert(filtered.size == 1)
            assert(filtered[0].nombre == "Hospital Municipal")
        }
    }
}
