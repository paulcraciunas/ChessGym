package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.domain.api.CountdownTimer
import com.paulcraciunas.domain.api.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.PuzzleRushResult
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.game.logic.impl.RealGameFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PuzzleRushViewModelTest {
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val puzzleSeries = FakeGetBufferedPuzzleSeries()
    private val onPuzzleRushComplete = FakeOnPuzzleRushComplete()
    private val countdownTimer = FakeCountdownTimer()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN puzzle available WHEN viewModel initialized THEN uiState is ready`() = runTest {
        // Given
        puzzleSeries.enqueue(buildStandardPuzzle())

        // When
        val underTest = buildVm()

        // Then
        assertTrue(underTest.uiState.value is PuzzleRushUiState.Ready)
        (underTest.uiState.value as PuzzleRushUiState.Ready).apply {
            assertEquals(DEFAULT_RATING, data.rating)
            assertEquals(Side.BLACK, data.player)
            assertEquals(PuzzleRushViewModel.DURATION_SECONDS, timeRemainingSeconds)
        }
    }

    @Test
    fun `GIVEN no puzzles available WHEN viewModel initialized THEN uiState is failed`() = runTest {
        // Given - no puzzles enqueued

        // When
        val underTest = buildVm()

        // Then
        assertTrue(underTest.uiState.value is PuzzleRushUiState.Failed)
    }

    @Test
    fun `GIVEN puzzleSeries throws WHEN viewModel initialized THEN uiState is failed`() = runTest {
        // Given
        puzzleSeries.withFailure(RuntimeException("boom"))

        // When
        val underTest = buildVm()

        // Then
        assertTrue(underTest.uiState.value is PuzzleRushUiState.Failed)
    }

    @Test
    fun `GIVEN ready state WHEN onSquareClicked THEN timer starts and state becomes playing`() = runTest {
        // Given
        puzzleSeries.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // When - click on player's piece to start the rush
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, countdownTimer.startCallCount)
        assertTrue(underTest.uiState.value is PuzzleRushUiState.Playing)
    }

    @Test
    fun `GIVEN playing state WHEN correct move made THEN puzzle progresses`() = runTest {
        // Given
        puzzleSeries.enqueue(buildStandardPuzzle())
        puzzleSeries.enqueue(buildStandardPuzzle(rating = 1250))
        val underTest = buildVm()

        // Start the rush
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // When - make correct moves to complete puzzle
        underTest.onSquareClicked("e5".loc()) // Make correct move (e7-e5)
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("b8".loc()) // Make correct move (e7-e5)
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("c6".loc()) // Make correct move (e7-e5)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - puzzle should have progressed, results should have one entry
        val state = underTest.uiState.value as PuzzleRushUiState.Playing
        assertEquals(1, state.results.size)
        assertTrue(state.results.first().success)
    }

    @Test
    fun `GIVEN playing state WHEN wrong move made THEN rush finishes`() = runTest {
        // Given
        puzzleSeries.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // Start the rush
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // When - make wrong move
        underTest.onSquareClicked("e6".loc()) // Wrong move (e7-e6 instead of e7-e5)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - rush should be finished
        assertTrue(underTest.uiState.value is PuzzleRushUiState.Finished)
        val finished = underTest.uiState.value as PuzzleRushUiState.Finished
        assertEquals(1, finished.results.size)
        assertFalse(finished.results.first().success)
        assertTrue(finished.showSummaryDialog)
    }

    @Test
    fun `GIVEN playing state WHEN time expires THEN rush finishes`() = runTest {
        // Given
        puzzleSeries.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // Start the rush
        underTest.onSquareClicked("e7".loc())

        // When - time expires
        countdownTimer.simulateTimeExpired()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(underTest.uiState.value is PuzzleRushUiState.Finished)
        assertEquals(1, countdownTimer.stopCallCount)
    }

    @Test
    fun `GIVEN rush finished WHEN onPuzzleRushComplete called THEN result is logged`() = runTest {
        // Given
        puzzleSeries.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // Start and fail the rush
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e6".loc()) // Wrong move
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull(onPuzzleRushComplete.lastResult)
        onPuzzleRushComplete.lastResult!!.apply {
            assertEquals(0, puzzlesSolved)
            assertEquals(1, puzzlesFailed)
        }
    }

    @Test
    fun `GIVEN finished state WHEN onPlayAgain THEN resets to ready`() = runTest {
        // Given
        puzzleSeries.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // Fail the rush
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e6".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Prepare next game
        puzzleSeries.enqueue(buildStandardPuzzle())

        // When
        underTest.onPlayAgain()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(underTest.uiState.value is PuzzleRushUiState.Ready)
        assertEquals(2, puzzleSeries.invokeCallCount) // invoke called twice (init + playAgain)
    }

    @Test
    fun `GIVEN finished state WHEN onDismissSummary THEN dialog is hidden`() = runTest {
        // Given
        puzzleSeries.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // Fail the rush
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e6".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue((underTest.uiState.value as PuzzleRushUiState.Finished).showSummaryDialog)

        // When
        underTest.onDismissSummary()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse((underTest.uiState.value as PuzzleRushUiState.Finished).showSummaryDialog)
    }

    @Test
    fun `GIVEN no selection WHEN onSquareClicked THEN selection and moves are marked`() = runTest {
        // Given
        puzzleSeries.enqueue(buildStandardPuzzle())
        val underTest = buildVm()
        underTest.onSquareClicked("e7".loc()) // Start rush

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val playingState = underTest.uiState.value as PuzzleRushUiState.Playing
        val boardData = playingState.data.boardData
        assertTrue(boardData.at("e7".loc()).piece?.isSelected == true)
    }

    private fun buildVm(): PuzzleRushViewModel {
        val underTest = PuzzleRushViewModel(
            puzzleSeries = puzzleSeries,
            onPuzzleRushComplete = onPuzzleRushComplete,
            countdownTimer = countdownTimer,
            gameFactory = RealGameFactory(),
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return underTest
    }

    private fun buildStandardPuzzle(
        rating: Int = DEFAULT_RATING,
        moves: List<String> = listOf("e2e4", "e7e5", "g1f3", "b8c6"),
    ): Puzzle = RealGameFactory().builder()
        .withDefaultBoard()
        .withRating(rating)
        .withMoves(moves)
        .buildPuzzle()

    private companion object {
        private const val DEFAULT_RATING: Int = 1200
    }
}

private class FakeGetBufferedPuzzleSeries : GetBufferedPuzzleSeries {
    private val puzzles = ArrayDeque<Puzzle>()
    private var exception: Exception? = null
    var invokeCallCount: Int = 0
        private set

    fun enqueue(puzzle: Puzzle) {
        puzzles.addLast(puzzle)
    }

    fun withFailure(exception: Exception) = apply {
        this.exception = exception
    }

    override fun invoke(batchSize: Int, ratingStart: Int, increment: Int) {
        invokeCallCount++
    }

    override suspend fun next(): Puzzle? {
        exception?.let { throw it }
        return puzzles.removeFirstOrNull()
    }
}

private class FakeOnPuzzleRushComplete : OnPuzzleRushComplete {
    var lastResult: PuzzleRushResult? = null
        private set

    override suspend fun invoke(result: PuzzleRushResult) {
        lastResult = result
    }
}

private class FakeCountdownTimer : CountdownTimer {
    private val _remainingSeconds = MutableStateFlow(PuzzleRushViewModel.DURATION_SECONDS)
    override val remainingSeconds: StateFlow<Int> = _remainingSeconds

    override val isExpired: Boolean
        get() = _remainingSeconds.value <= 0

    var startCallCount: Int = 0
        private set
    var stopCallCount: Int = 0
        private set

    override fun set(durationSeconds: Int) {
        _remainingSeconds.value = durationSeconds
    }

    override fun start(scope: CoroutineScope) {
        startCallCount++
    }

    override fun stop() {
        stopCallCount++
    }

    override fun elapsedMillis(): Long = 60_000L

    fun simulateTimeExpired() {
        _remainingSeconds.value = 0
    }
}
