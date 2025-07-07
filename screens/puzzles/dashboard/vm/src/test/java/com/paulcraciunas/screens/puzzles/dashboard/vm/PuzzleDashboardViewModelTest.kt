package com.paulcraciunas.screens.puzzles.dashboard.vm

import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserLocalDataSource
import com.paulcraciunas.user.api.UserRemoteDataSource
import com.paulcraciunas.user.impl.UserRepositoryImpl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PuzzleDashboardViewModelTest {

    private val fakeLocalDataSource = FakeUserLocalDataSource()
    private val fakeRemoteDataSource = FakeUserRemoteDataSource()
    private val userRepository = UserRepositoryImpl(fakeLocalDataSource, fakeRemoteDataSource)

    private lateinit var underTest: PuzzleDashboardViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
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
        fakeLocalDataSource.saveUser(user)

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
        fakeLocalDataSource.saveUser(user)

        // When
        underTest = PuzzleDashboardViewModel(userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertEquals(0, uiState.failedPuzzlesCount)
    }

    private fun setupViewModel() = runTest {
        val user = User()
        fakeLocalDataSource.saveUser(user)
        underTest = PuzzleDashboardViewModel(userRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }
}

// Fake implementations for testing
private class FakeUserLocalDataSource : UserLocalDataSource {
    private var user = User()

    override fun userUpdates(): Flow<User> = flowOf(user)
    override suspend fun getUser(): User = user
    override suspend fun saveUser(user: User) {
        this.user = user
    }

    override suspend fun updateUser(updater: (User) -> User) {
        this.user = updater(this.user)
    }

    override suspend fun clearUserData() {
        this.user = User()
    }
}

private class FakeUserRemoteDataSource : UserRemoteDataSource {
    override suspend fun getUser(userId: String): User = throw NotImplementedError()
    override suspend fun updateUser(user: User) = Unit
    override suspend fun addToHistory(userId: String, history: List<User.HistoryItem>) = Unit
    override suspend fun signIn(auth: User.AuthenticationState, token: String): User = User(authentication = auth)
    override suspend fun deleteUser(userId: String) = Unit
}
