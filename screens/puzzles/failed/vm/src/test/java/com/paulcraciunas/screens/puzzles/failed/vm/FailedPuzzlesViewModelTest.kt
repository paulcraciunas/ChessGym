package com.paulcraciunas.screens.puzzles.failed.vm

import com.paulcraciunas.domain.api.general.FakeTimer
import com.paulcraciunas.domain.api.puzzles.FakeGetPuzzleFen
import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.domain.api.puzzles.OnFailedPuzzleComplete
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.game.logic.impl.RealGameFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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
internal class FailedPuzzlesViewModelTest {
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val getFailedPuzzles = FakeGetFailedPuzzles()
    private val onFailedPuzzleComplete = FakeOnFailedPuzzleComplete()
    private val getPuzzleFen = FakeGetPuzzleFen()
    private val timer = FakeTimer()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN puzzles available WHEN viewModel initialized THEN uiState is playing`() = runTest {
        // Given
        getFailedPuzzles.enqueue(buildStandardPuzzle())

        // When
        val underTest = buildVm()

        // Then
        assertTrue(underTest.uiState.value is FailedPuzzlesUiState.Playing)
        (underTest.uiState.value as FailedPuzzlesUiState.Playing).apply {
            assertEquals(DEFAULT_RATING, data.rating)
            assertEquals(Side.BLACK, data.player)
            assertEquals(0, progress.solved)
            assertEquals(1, progress.total)
        }
    }

    @Test
    fun `GIVEN no puzzles available WHEN viewModel initialized THEN uiState is empty`() = runTest {
        // Given - no puzzles enqueued, totalCount = 0

        // When
        val underTest = buildVm()

        // Then
        assertTrue(underTest.uiState.value is FailedPuzzlesUiState.Empty)
    }

    @Test
    fun `GIVEN getFailedPuzzles throws WHEN viewModel initialized THEN uiState is failed`() = runTest {
        // Given
        getFailedPuzzles.withFailure(RuntimeException("boom"))

        // When
        val underTest = buildVm()

        // Then
        assertTrue(underTest.uiState.value is FailedPuzzlesUiState.Failed)
    }

    @Test
    fun `GIVEN playing state WHEN correct move made THEN puzzle progresses`() = runTest {
        // Given
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        getFailedPuzzles.enqueue(buildStandardPuzzle(rating = 1250))
        val underTest = buildVm()

        // When - make correct moves to complete puzzle
        underTest.onSquareClicked("e7".loc()) // Select piece
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e5".loc()) // Make correct move (e7-e5)
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("b8".loc()) // Select piece
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("c6".loc()) // Make correct move (b8-c6)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - puzzle should have progressed, results should have one entry
        val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
        assertEquals(1, state.results.size)
        assertTrue(state.results.first().success)
        assertEquals(1, state.progress.solved)
    }

    @Test
    fun `GIVEN puzzle solved WHEN handleMoveResult THEN onFailedPuzzleComplete is called`() = runTest {
        // Given
        val puzzleId = 42
        getFailedPuzzles.enqueue(buildStandardPuzzle(id = puzzleId))
        getFailedPuzzles.enqueue(buildStandardPuzzle(rating = 1250))
        val underTest = buildVm()

        // When - solve the puzzle
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e5".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("b8".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("c6".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(puzzleId, onFailedPuzzleComplete.lastCompletedId)
    }

    @Test
    fun `GIVEN playing state WHEN wrong move made THEN puzzle fails and moves to next`() = runTest {
        // Given
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        getFailedPuzzles.enqueue(buildStandardPuzzle(rating = 1250))
        val underTest = buildVm()

        // When - make wrong move
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e6".loc()) // Wrong move (e7-e6 instead of e7-e5)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should move to next puzzle, result should show failure
        val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
        assertEquals(1, state.results.size)
        assertFalse(state.results.first().success)
        assertEquals(0, state.progress.solved) // Solved count should not increase
    }

    @Test
    fun `GIVEN wrong move WHEN handleMoveResult THEN onFailedPuzzleComplete is NOT called`() = runTest {
        // Given
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        getFailedPuzzles.enqueue(buildStandardPuzzle(rating = 1250))
        val underTest = buildVm()

        // When - make wrong move
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e6".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(null, onFailedPuzzleComplete.lastCompletedId)
    }

    @Test
    fun `GIVEN last puzzle completed WHEN handleMoveResult THEN uiState is finished`() = runTest {
        // Given - only one puzzle
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // When - complete the puzzle
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e5".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("b8".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("c6".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(underTest.uiState.value is FailedPuzzlesUiState.Finished)
        val finished = underTest.uiState.value as FailedPuzzlesUiState.Finished
        assertTrue(finished.showCompletionDialog)
        assertEquals(1, finished.results.size)
        assertTrue(finished.results.first().success)
    }

    @Test
    fun `GIVEN finished state WHEN onDismissCompletion THEN dialog is hidden`() = runTest {
        // Given - only one puzzle
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // Complete the puzzle
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e5".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("b8".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("c6".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue((underTest.uiState.value as FailedPuzzlesUiState.Finished).showCompletionDialog)

        // When
        underTest.onDismissCompletion()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse((underTest.uiState.value as FailedPuzzlesUiState.Finished).showCompletionDialog)
    }

    @Test
    fun `GIVEN no selection WHEN onSquareClicked THEN selection and moves are marked`() = runTest {
        // Given
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // When
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val playingState = underTest.uiState.value as FailedPuzzlesUiState.Playing
        val boardData = playingState.data.boardData
        assertTrue(boardData.at("e7".loc()).piece?.isSelected == true)
    }

    @Test
    fun `GIVEN selection WHEN same square clicked THEN clears selection`() = runTest {
        // Given
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        // Select a piece
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // When - click same square
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val playingState = underTest.uiState.value as FailedPuzzlesUiState.Playing
        val boardData = playingState.data.boardData
        assertTrue(boardData.at("e7".loc()).piece?.isSelected != true)
    }

    @Test
    fun `GIVEN multiple puzzles WHEN all completed THEN shows total progress`() = runTest {
        // Given
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        getFailedPuzzles.enqueue(buildStandardPuzzle(rating = 1250))
        val underTest = buildVm()

        // Complete first puzzle
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e5".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("b8".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("c6".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Complete second puzzle
        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e5".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("b8".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("c6".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val finished = underTest.uiState.value as FailedPuzzlesUiState.Finished
        assertEquals(2, finished.progress.solved)
        assertEquals(2, finished.progress.total)
        assertEquals(2, finished.results.size)
    }

    @Test
    fun `GIVEN loading state WHEN onSquareClicked THEN does nothing`() = runTest {
        // Given - ViewModel still loading
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        val underTest = FailedPuzzlesViewModel(
            getFailedPuzzles = getFailedPuzzles,
            onFailedPuzzleComplete = onFailedPuzzleComplete,
            getPuzzleFen = getPuzzleFen,
            timer = timer,
            puzzleInteractor = RealGameFactory().puzzleInteractor(),
        )
        // Don't advance dispatcher - state is still loading

        // When
        underTest.onSquareClicked("e7".loc())

        // Then - should still be loading (no crash)
        assertTrue(underTest.uiState.value is FailedPuzzlesUiState.Loading)
    }

    @Test
    fun `GIVEN finished state WHEN onSquareClicked THEN does nothing`() = runTest {
        // Given - only one puzzle, complete it
        getFailedPuzzles.enqueue(buildStandardPuzzle())
        val underTest = buildVm()

        underTest.onSquareClicked("e7".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("e5".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("b8".loc())
        testDispatcher.scheduler.advanceUntilIdle()
        underTest.onSquareClicked("c6".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(underTest.uiState.value is FailedPuzzlesUiState.Finished)

        // When
        underTest.onSquareClicked("e2".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should still be finished
        assertTrue(underTest.uiState.value is FailedPuzzlesUiState.Finished)
    }

    private fun buildVm(): FailedPuzzlesViewModel {
        val underTest = FailedPuzzlesViewModel(
            getFailedPuzzles = getFailedPuzzles,
            onFailedPuzzleComplete = onFailedPuzzleComplete,
            getPuzzleFen = getPuzzleFen,
            timer = timer,
            puzzleInteractor = RealGameFactory().puzzleInteractor(),
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return underTest
    }

    private fun buildStandardPuzzle(
        id: Int? = DEFAULT_ID,
        rating: Int = DEFAULT_RATING,
        moves: List<String> = listOf("e2e4", "e7e5", "g1f3", "b8c6"),
    ): Puzzle = RealGameFactory().builder()
        .withDefaultBoard()
        .withId(id)
        .withRating(rating)
        .withMoves(moves)
        .buildPuzzle()

    private companion object {
        private const val DEFAULT_ID: Int = 42
        private const val DEFAULT_RATING: Int = 1200
    }
}

private class FakeGetFailedPuzzles : GetFailedPuzzles {
    private val puzzles = ArrayDeque<Puzzle>()
    private var exception: Exception? = null
    private var _totalCount: Int = 0

    fun enqueue(puzzle: Puzzle) {
        puzzles.addLast(puzzle)
        _totalCount++
    }

    fun withFailure(exception: Exception) = apply {
        this.exception = exception
    }

    override suspend fun load(batchSize: Int) {
        exception?.let { throw it }
    }

    override suspend fun next(): Puzzle? {
        exception?.let { throw it }
        return puzzles.removeFirstOrNull()
    }

    override fun totalCount(): Int = _totalCount

    override fun remainingCount(): Int = puzzles.size
}

private class FakeOnFailedPuzzleComplete : OnFailedPuzzleComplete {
    var lastCompletedId: Int? = null
        private set

    override suspend fun invoke(puzzleId: Int, timeSpentMillis: Long) {
        lastCompletedId = puzzleId
    }
}
