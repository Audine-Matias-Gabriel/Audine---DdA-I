package com.audine.dedalo.auth.ui

import app.cash.turbine.test
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.auth.domain.AuthUiState
import io.mockk.coEvery
import io.mockk.coVerify
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
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mockk()

    private val user = UserEntity(
        id = "user123",
        displayName = "Test User",
        email = "test@example.com",
        photoUrl = "https://example.com/avatar.jpg"
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
        every { authRepository.observeAuthState() } returns flowOf(AuthRepository.AuthState.Loading)

        val viewModel = AuthViewModel(authRepository)

        assert(viewModel.uiState.value is AuthUiState.Loading)
    }

    @Test
    fun `when Authenticated state maps to Authenticated UiState`() = runTest(testDispatcher) {
        every { authRepository.observeAuthState() } returns flowOf(AuthRepository.AuthState.Authenticated(user))

        val viewModel = AuthViewModel(authRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is AuthUiState.Authenticated)
            assert((state as AuthUiState.Authenticated).user == user)
        }
    }

    @Test
    fun `when Unauthenticated state maps to Unauthenticated UiState`() = runTest(testDispatcher) {
        every { authRepository.observeAuthState() } returns flowOf(AuthRepository.AuthState.Unauthenticated)

        val viewModel = AuthViewModel(authRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is AuthUiState.Unauthenticated)
        }
    }

    @Test
    fun `signOut calls authRepository signOut and clearUser`() = runTest(testDispatcher) {
        every { authRepository.observeAuthState() } returns flowOf(AuthRepository.AuthState.Loading)
        coEvery { authRepository.signOut() } returns Unit
        coEvery { authRepository.clearUser() } returns Unit

        val viewModel = AuthViewModel(authRepository)

        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { authRepository.signOut() }
        coVerify { authRepository.clearUser() }
    }
}
