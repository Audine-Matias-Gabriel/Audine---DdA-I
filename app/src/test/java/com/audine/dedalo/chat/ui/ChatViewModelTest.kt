package com.audine.dedalo.chat.ui

import app.cash.turbine.test
import com.audine.dedalo.chat.data.ChatRepository
import com.audine.dedalo.chat.domain.ChatUiState
import com.audine.dedalo.chat.domain.model.ChatMessage
import io.mockk.coEvery
import io.mockk.coVerify
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
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val chatRepository: ChatRepository = mockk()

    private val messages = listOf(
        ChatMessage(id = 1, role = "user", content = "Hola"),
        ChatMessage(id = 2, role = "model", content = "Hola, soy Dedalo IA")
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
        every { chatRepository.messages } returns emptyFlow()

        val viewModel = ChatViewModel(chatRepository)

        assert(viewModel.uiState.value is ChatUiState.Loading)
    }

    @Test
    fun `when messages emit state becomes Success`() = runTest(testDispatcher) {
        every { chatRepository.messages } returns flowOf(messages)

        val viewModel = ChatViewModel(chatRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is ChatUiState.Success)
            val success = state as ChatUiState.Success
            assert(success.messages == messages)
            assert(!success.isLoadingResponse)
        }
    }

    @Test
    fun `sendMessage calls repository and sets loading state`() = runTest(testDispatcher) {
        every { chatRepository.messages } returns flowOf(messages)
        coEvery { chatRepository.sendMessage(any()) } returns Unit

        val viewModel = ChatViewModel(chatRepository)

        viewModel.sendMessage("Test message")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { chatRepository.sendMessage("Test message") }
    }

    @Test
    fun `clearHistory calls repository`() = runTest(testDispatcher) {
        every { chatRepository.messages } returns emptyFlow()
        coEvery { chatRepository.clearHistory() } returns Unit

        val viewModel = ChatViewModel(chatRepository)

        viewModel.clearHistory()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { chatRepository.clearHistory() }
    }

    @Test
    fun `sendMessage empty string does nothing`() = runTest(testDispatcher) {
        every { chatRepository.messages } returns flowOf(messages)

        val viewModel = ChatViewModel(chatRepository)

        viewModel.sendMessage("   ")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { chatRepository.sendMessage(any()) }
    }
}
