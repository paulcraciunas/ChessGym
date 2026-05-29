package com.paulcraciunas.screens.boardvis.squares.vm

import com.paulcraciunas.domain.api.general.FakeCountdownTimer
import com.paulcraciunas.domain.api.boardvis.FindSquareResult
import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.domain.api.GenerateRandomLoci
import com.paulcraciunas.domain.api.boardvis.OnFindSquareComplete
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.screens.data.SideSelection
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class FindTheSquareViewModelTest {
    private val userRepository = FakeUserRepository()
    private val fakeCountdownTimer = FakeCountdownTimer()
    private val fakeRandomFactory = FixedRandomFactory()
    private val fakeGenerateRandomLoci = FakeGenerateRandomLoci()
    private val fakeOnFindSquareComplete = FakeOnFindSquareComplete()

    private lateinit var underTest: FindTheSquareViewModel

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
    fun `GIVEN initial state WHEN viewModel created THEN uiState is Setup`() = runTest {
        // Given/When
        setupViewModel()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.Setup)
        assertEquals(SideSelection.WHITE, (uiState as FindTheSquareUiState.Setup).selectedSide)
    }

    @Test
    fun `GIVEN setup state WHEN onSideSelected with BLACK THEN selectedSide updated`() = runTest {
        // Given
        setupViewModel()

        // When
        underTest.onSideSelected(SideSelection.BLACK)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.Setup)
        assertEquals(SideSelection.BLACK, (uiState as FindTheSquareUiState.Setup).selectedSide)
    }

    @Test
    fun `GIVEN setup state with WHITE WHEN onPlayClicked THEN game starts with white orientation`() = runTest {
        // Given
        setupViewModel()
        fakeGenerateRandomLoci.nextLocus = Locus.e4

        // When
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.Playing)
        val playingState = uiState as FindTheSquareUiState.Playing
        assertEquals(Locus.e4, playingState.currentSquare)
        assertEquals(0, playingState.score)
        assertEquals(Side.WHITE,playingState.orientation)
        assertTrue(fakeCountdownTimer.isRunning)
    }

    @Test
    fun `GIVEN setup state with BLACK WHEN onPlayClicked THEN game starts with black orientation`() = runTest {
        // Given
        setupViewModel()
        underTest.onSideSelected(SideSelection.BLACK)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeGenerateRandomLoci.nextLocus = Locus.d5

        // When
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.Playing)
        val playingState = uiState as FindTheSquareUiState.Playing
        assertEquals(Side.BLACK,playingState.orientation)
    }

    @Test
    fun `GIVEN setup state with RANDOM WHEN onPlayClicked THEN orientation is random`() = runTest {
        // Given
        setupViewModel()
        underTest.onSideSelected(SideSelection.RANDOM)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeRandomFactory.returnValue = 1 // Will result in black orientation
        fakeGenerateRandomLoci.nextLocus = Locus.a1

        // When
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.Playing)
        val playingState = uiState as FindTheSquareUiState.Playing
        assertEquals(Side.BLACK,playingState.orientation)
    }

    @Test
    fun `GIVEN playing state WHEN correct square clicked THEN score increments`() = runTest {
        // Given
        setupViewModel()
        fakeGenerateRandomLoci.nextLocus = Locus.e4
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Set next locus for after correct answer
        fakeGenerateRandomLoci.nextLocus = Locus.b2

        // When
        underTest.onSquareClicked(Locus.e4)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.Playing)
        val playingState = uiState as FindTheSquareUiState.Playing
        assertEquals(1, playingState.score)
        assertEquals(Locus.b2, playingState.currentSquare)
    }

    @Test
    fun `GIVEN playing state WHEN wrong square clicked THEN showError is true`() = runTest {
        // Given
        setupViewModel()
        fakeGenerateRandomLoci.nextLocus = Locus.e4
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        underTest.onSquareClicked(Locus.a1) // Wrong square
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.Playing)
        val playingState = uiState as FindTheSquareUiState.Playing
        assertEquals(0, playingState.score) // Score unchanged
        assertTrue(playingState.showError)
    }

    @Test
    fun `GIVEN error showing WHEN onErrorShown called THEN showError is false`() = runTest {
        // Given
        setupViewModel()
        fakeGenerateRandomLoci.nextLocus = Locus.e4
        underTest.onPlayClicked()
        underTest.onSquareClicked(Locus.a1) // Wrong square
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        underTest.onErrorShown()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.Playing)
        val playingState = uiState as FindTheSquareUiState.Playing
        assertFalse(playingState.showError)
    }

    @Test
    fun `GIVEN playing state WHEN timer expires THEN game ends`() = runTest {
        // Given
        setupViewModel()
        fakeGenerateRandomLoci.nextLocus = Locus.e4
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Simulate clicking correct squares
        fakeGenerateRandomLoci.nextLocus = Locus.b2
        underTest.onSquareClicked(Locus.e4)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - simulate timer expiry
        fakeCountdownTimer.advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.GameOver)
        val gameOverState = uiState as FindTheSquareUiState.GameOver
        assertEquals(1, gameOverState.score)
        assertFalse(fakeCountdownTimer.isRunning)
    }

    @Test
    fun `GIVEN score higher than high score WHEN game ends THEN isNewHighScore is true`() = runTest {
        // Given
        setupViewModel(user = User(highScores = User.HighScores(findTheSquare = 5)))

        fakeGenerateRandomLoci.nextLocus = Locus.e4
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Simulate getting score of 6
        repeat(6) {
            val currentSquare = (underTest.uiState.value as FindTheSquareUiState.Playing).currentSquare
            fakeGenerateRandomLoci.nextLocus = Locus.from(File.entries[it], Rank.entries[it])
            underTest.onSquareClicked(currentSquare)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        // When - simulate timer expiry
        fakeCountdownTimer.advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.GameOver)
        val gameOverState = uiState as FindTheSquareUiState.GameOver
        assertTrue(gameOverState.isNewHighScore)
        assertEquals(5, gameOverState.previousHighScore)
    }

    @Test
    fun `GIVEN score lower than high score WHEN game ends THEN isNewHighScore is false`() = runTest {
        // Given
        setupViewModel(User(highScores = User.HighScores(findTheSquare = 10)))

        fakeGenerateRandomLoci.nextLocus = Locus.e4
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // When - simulate timer expiry with score of 0
        fakeCountdownTimer.advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.GameOver)
        val gameOverState = uiState as FindTheSquareUiState.GameOver
        assertFalse(gameOverState.isNewHighScore)
    }

    @Test
    fun `GIVEN game over WHEN onPlayAgain called THEN returns to setup state`() = runTest {
        // Given
        setupViewModel()
        fakeGenerateRandomLoci.nextLocus = Locus.e4
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()
        fakeCountdownTimer.advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        underTest.onPlayAgain()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is FindTheSquareUiState.Setup)
    }

    @Test
    fun `GIVEN game ends WHEN invoke THEN onFindSquareComplete is called`() = runTest {
        // Given
        setupViewModel()
        fakeGenerateRandomLoci.nextLocus = Locus.e4
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Click one correct square
        fakeGenerateRandomLoci.nextLocus = Locus.b2
        underTest.onSquareClicked(Locus.e4)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - timer expires
        fakeCountdownTimer.advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(fakeOnFindSquareComplete.invoked)
        assertEquals(1, fakeOnFindSquareComplete.lastResult?.score)
    }

    private fun setupViewModel(user: User = User()) = runTest {
        userRepository.local.saveUser(user)
        underTest = FindTheSquareViewModel(
            generateRandomLoci = fakeGenerateRandomLoci,
            onFindSquareComplete = fakeOnFindSquareComplete,
            countdownTimer = fakeCountdownTimer,
            userRepository = userRepository,
            randomFactory = fakeRandomFactory,
            gameDuration = DefaultGameDuration(),
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }
}

private class FakeGenerateRandomLoci : GenerateRandomLoci {
    var nextLocus: Locus = Locus.a1
    override fun invoke(): Locus = nextLocus
}

private class FakeOnFindSquareComplete : OnFindSquareComplete {
    var invoked = false
        private set
    var lastResult: FindSquareResult? = null
        private set

    override suspend fun invoke(result: FindSquareResult) {
        invoked = true
        lastResult = result
    }
}
