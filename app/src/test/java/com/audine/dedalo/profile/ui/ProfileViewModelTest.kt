package com.audine.dedalo.profile.ui

import android.net.Uri
import app.cash.turbine.test
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.profile.data.GalleryImageEntity
import com.audine.dedalo.profile.data.ProfileRepository
import com.audine.dedalo.profile.domain.ProfileUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val authRepository: AuthRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()

    private val user = UserEntity(
        id = "user123",
        displayName = "Test User",
        email = "test@example.com",
        photoUrl = "https://example.com/avatar.jpg"
    )

    private val galleryImages = listOf(
        GalleryImageEntity(id = "img1", userId = "user123", imageUrl = "https://example.com/1.jpg"),
        GalleryImageEntity(id = "img2", userId = "user123", imageUrl = "https://example.com/2.jpg")
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
        every { authRepository.currentUser } returns MutableStateFlow(null)

        val viewModel = ProfileViewModel(authRepository, profileRepository)

        assert(viewModel.uiState.value is ProfileUiState.Loading)
    }

    @Test
    fun `when user emits gallery loads and state becomes Success`() = runTest(testDispatcher) {
        val userFlow = MutableStateFlow(user)
        every { authRepository.currentUser } returns userFlow
        every { profileRepository.observeGallery(user.id) } returns flowOf(galleryImages)

        val viewModel = ProfileViewModel(authRepository, profileRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ProfileUiState.Success)
            val success = state as ProfileUiState.Success
            assert(success.user == user)
            assert(success.galleryImages == galleryImages)
        }
    }

    @Test
    fun `when user is null state remains Loading`() = runTest(testDispatcher) {
        val userFlow = MutableStateFlow<UserEntity?>(null)
        every { authRepository.currentUser } returns userFlow

        val viewModel = ProfileViewModel(authRepository, profileRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ProfileUiState.Loading)
        }
    }

    @Test
    fun `uploadGalleryImage delegates to repository`() = runTest(testDispatcher) {
        val uri = mockk<Uri>()
        val userFlow = MutableStateFlow(user)
        every { authRepository.currentUser } returns userFlow
        every { profileRepository.observeGallery(any()) } returns emptyFlow()
        coEvery { profileRepository.uploadGalleryImage(user.id, uri) } returns "url"

        val viewModel = ProfileViewModel(authRepository, profileRepository)

        viewModel.uploadGalleryImage(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { profileRepository.uploadGalleryImage(user.id, uri) }
    }

    @Test
    fun `uploadAvatar delegates to repository`() = runTest(testDispatcher) {
        val uri = mockk<Uri>()
        val userFlow = MutableStateFlow(user)
        every { authRepository.currentUser } returns userFlow
        every { profileRepository.observeGallery(any()) } returns emptyFlow()
        coEvery { profileRepository.uploadAvatar(user.id, uri) } returns "url"

        val viewModel = ProfileViewModel(authRepository, profileRepository)

        viewModel.uploadAvatar(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { profileRepository.uploadAvatar(user.id, uri) }
    }

    @Test
    fun `signOut calls authRepository and invokes callback`() = runTest(testDispatcher) {
        val userFlow = MutableStateFlow(user)
        every { authRepository.currentUser } returns userFlow
        every { profileRepository.observeGallery(any()) } returns emptyFlow()
        coEvery { authRepository.signOut() } returns Unit
        coEvery { authRepository.clearUser() } returns Unit

        val viewModel = ProfileViewModel(authRepository, profileRepository)
        var callbackInvoked = false

        viewModel.signOut { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { authRepository.signOut() }
        coVerify { authRepository.clearUser() }
        assert(callbackInvoked)
    }

    @Test
    fun `gallery updates reflect in uiState`() = runTest(testDispatcher) {
        val userFlow = MutableStateFlow(user)
        val galleryFlow = MutableStateFlow(emptyList<GalleryImageEntity>())
        every { authRepository.currentUser } returns userFlow
        every { profileRepository.observeGallery(user.id) } returns galleryFlow

        val viewModel = ProfileViewModel(authRepository, profileRepository)

        galleryFlow.value = galleryImages
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state is ProfileUiState.Success)
        assert((state as ProfileUiState.Success).galleryImages == galleryImages)
    }
}
