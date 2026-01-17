package com.paulcraciunas.screens.puzzles.dashboard.vm

import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PuzzleDashboardViewModelTest {
    private val userRepository = FakeUserRepository()

    private lateinit var underTest: PuzzleDashboardViewModel

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
    fun `GIVEN userRepository returns user WHEN viewModel initialized THEN uiState updated with user data`() = runTest {
        // Given
        val user = User(
            ratings = User.Ratings(current = 1650),
            failedPuzzles = listOf(1, 2, 3)
        )
        userRepository.local.saveUser(user)

        // When
        underTest = PuzzleDashboardViewModel(userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertEquals(1650, uiState.userRating)
        assertEquals(3, uiState.failedPuzzlesCount)
        assertEquals(false, uiState.isLoading)
    }

    @Test
    fun `GIVEN default configuration WHEN onPuzzleRushTimeChanged called THEN time limit updated`() = runTest {
        // Given
        setupViewModel()
        val newTimeLimit = PuzzleDashboardUiState.PuzzleRushConfig.TimeLimit.FIVE_MINUTES

        // When
        underTest.onPuzzleRushTimeChanged(newTimeLimit)

        // Then
        val uiState = underTest.uiState.value
        assertEquals(newTimeLimit, uiState.puzzleRushConfig.timeLimit)
        assertEquals(
            PuzzleDashboardUiState.PuzzleRushConfig.MistakesAllowed.TWO_MISTAKES,
            uiState.puzzleRushConfig.mistakesAllowed
        )
    }

    @Test
    fun `GIVEN default configuration WHEN onPuzzleRushMistakesChanged called THEN mistakes allowed updated`() = runTest {
        // Given
        setupViewModel()
        val newMistakesAllowed = PuzzleDashboardUiState.PuzzleRushConfig.MistakesAllowed.ZERO_MISTAKES

        // When
        underTest.onPuzzleRushMistakesChanged(newMistakesAllowed)

        // Then
        val uiState = underTest.uiState.value
        assertEquals(
            PuzzleDashboardUiState.PuzzleRushConfig.TimeLimit.THREE_MINUTES,
            uiState.puzzleRushConfig.timeLimit
        )
        assertEquals(newMistakesAllowed, uiState.puzzleRushConfig.mistakesAllowed)
    }

    @Test
    fun `GIVEN puzzle mode selected WHEN onPuzzleModeSelected called THEN navigation callback invoked`() = runTest {
        // Given
        setupViewModel()
        val puzzleMode = PuzzleMode.RatedPuzzle
        var callbackInvoked = false
        var receivedMode: PuzzleMode? = null

        // When
        underTest.onPuzzleModeSelected(puzzleMode) { mode ->
            callbackInvoked = true
            receivedMode = mode
        }

        // Then
        assertEquals(true, callbackInvoked)
        assertEquals(puzzleMode, receivedMode)
    }

    @Test
    fun `GIVEN puzzle rush mode selected WHEN onPuzzleModeSelected called THEN navigation callback receives correct config`() = runTest {
        // Given
        setupViewModel()
        underTest.onPuzzleRushTimeChanged(PuzzleDashboardUiState.PuzzleRushConfig.TimeLimit.FIVE_MINUTES)
        underTest.onPuzzleRushMistakesChanged(PuzzleDashboardUiState.PuzzleRushConfig.MistakesAllowed.ZERO_MISTAKES)
        
        val expectedConfig = PuzzleDashboardUiState.PuzzleRushConfig(
            timeLimit = PuzzleDashboardUiState.PuzzleRushConfig.TimeLimit.FIVE_MINUTES,
            mistakesAllowed = PuzzleDashboardUiState.PuzzleRushConfig.MistakesAllowed.ZERO_MISTAKES
        )
        val puzzleMode = PuzzleMode.PuzzleRush(expectedConfig)
        var receivedMode: PuzzleMode? = null

        // When
        underTest.onPuzzleModeSelected(puzzleMode) { mode ->
            receivedMode = mode
        }

        // Then
        assertEquals(puzzleMode, receivedMode)
        assertEquals(
            expectedConfig.timeLimit,
            (receivedMode as PuzzleMode.PuzzleRush).config.timeLimit
        )
        assertEquals(
            expectedConfig.mistakesAllowed,
            (receivedMode as PuzzleMode.PuzzleRush).config.mistakesAllowed
        )
    }

    @Test
    fun `GIVEN user with no failed puzzles WHEN viewModel initialized THEN failedPuzzlesCount is zero`() = runTest {
        // Given
        val user = User(
            ratings = User.Ratings(current = 1200),
            failedPuzzles = emptyList()
        )
        userRepository.local.saveUser(user)

        // When
        underTest = PuzzleDashboardViewModel(userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertEquals(0, uiState.failedPuzzlesCount)
    }

    private fun setupViewModel() = runTest {
        val user = User()
        userRepository.local.saveUser(user)
        underTest = PuzzleDashboardViewModel(userRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
