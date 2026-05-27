package com.paulcraciunas.screens.boardvis.dashboard.vm

import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class BoardVisDashboardViewModelTest {
    private val userRepository = FakeUserRepository()

    private lateinit var underTest: BoardVisDashboardViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN userRepository returns user WHEN viewModel initialized THEN uiState updated with high score`() = runTest {
        // Given
        setupViewModel(user = User(highScores = User.HighScores(findTheSquare = 42)))

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertEquals(42, uiState.findSquareHighScore)
        assertEquals(false, uiState.isLoading)
    }

    @Test
    fun `GIVEN user with zero high score WHEN viewModel initialized THEN high score is zero`() = runTest {
        // Given
        setupViewModel()

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertEquals(0, uiState.findSquareHighScore)
    }

    @Test
    fun `GIVEN mode selected WHEN onModeSelected called THEN navigation callback invoked`() = runTest {
        // Given
        setupViewModel()
        val mode = BoardVisMode.FindTheSquare
        var callbackInvoked = false
        var receivedMode: BoardVisMode? = null

        // When
        underTest.onModeSelected(mode) { selectedMode ->
            callbackInvoked = true
            receivedMode = selectedMode
        }

        // Then
        assertEquals(true, callbackInvoked)
        assertEquals(mode, receivedMode)
    }

    @Test
    fun `GIVEN MoveThePiece mode WHEN onModeSelected called THEN navigation callback invoked with MoveThePiece`() = runTest {
        // Given
        setupViewModel()
        val mode = BoardVisMode.MoveThePiece
        var receivedMode: BoardVisMode? = null

        // When
        underTest.onModeSelected(mode) { selectedMode ->
            receivedMode = selectedMode
        }

        // Then
        assertEquals(BoardVisMode.MoveThePiece, receivedMode)
    }

    private fun setupViewModel(user: User = User()) = runTest {
        userRepository.local.saveUser(user)
        underTest = BoardVisDashboardViewModel(userRepository)
        advanceUntilIdle()
        observeUiState()
    }

    private fun TestScope.observeUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            underTest.uiState.collect {}
        }
    }
}
