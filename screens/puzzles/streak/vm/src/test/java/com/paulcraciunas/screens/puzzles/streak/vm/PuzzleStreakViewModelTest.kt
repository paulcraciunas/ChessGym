package com.paulcraciunas.screens.puzzles.streak.vm

import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakPuzzleComplete
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PuzzleStreakViewModelTest {
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val getStreakPuzzle = FakeGetStreakPuzzle()
    private val onStreakPuzzleComplete = FakeOnStreakPuzzleComplete()
    private val onStreakComplete = FakeOnStreakComplete()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN puzzle loaded WHEN viewModel initialized THEN uiState is playing`() = runTest {
        // Given/When
        val underTest = buildVm(buildStandardPuzzle())

        // Then
        assertTrue(underTest.uiState.value is PuzzleStreakUiState.Playing)
        (underTest.uiState.value as PuzzleStreakUiState.Playing).apply {
            assertEquals(DEFAULT_RATING, data.rating)
            assertEquals(Side.BLACK, data.player)
            assertEquals(0, streakCount)
            assertNull(promotion)
        }
    }

    @Test
    fun `GIVEN getStreakPuzzle throws WHEN viewModel initialized THEN uiState is failed`() = runTest {
        // Given
        getStreakPuzzle.withFailure(RuntimeException("boom"))

        // When
        val underTest = buildVm(buildStandardPuzzle())

        // Then
        assertTrue(underTest.uiState.value is PuzzleStreakUiState.Failed)
    }

    @Test
    fun `GIVEN existing streak WHEN viewModel initialized THEN shows current streak count`() = runTest {
        // Given
        val existingStreak = 5

        // When
        val underTest = buildVm(buildStandardPuzzle(), currentStreakCount = existingStreak)

        // Then
        val playingState = underTest.uiState.value as PuzzleStreakUiState.Playing
        assertEquals(existingStreak, playingState.streakCount)
    }

    @Test
    fun `GIVEN no selection WHEN onSquareClicked THEN selection and moves are marked`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())

        // When
        underTest.onSquareClicked("e7".loc())

        // Then
        val playingState = underTest.uiState.value as PuzzleStreakUiState.Playing
        val selectedSquare = playingState.data.boardData.at(Rank.`7`, File.e)
        assertEquals(true, selectedSquare.piece?.isSelected)
        assertTrue(playingState.data.boardData.at(Rank.`5`, File.e).canMoveTo)
    }

    @Test
    fun `GIVEN selected square WHEN onSquareClicked invalid target THEN selection is cleared`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())
        underTest.onSquareClicked("e7".loc())

        // When
        underTest.onSquareClicked("e4".loc())

        // Then
        val playingState = underTest.uiState.value as PuzzleStreakUiState.Playing
        val selectedSquare = playingState.data.boardData.at(Rank.`7`, File.e)
        assertEquals(false, selectedSquare.piece?.isSelected)
        assertFalse(playingState.data.boardData.at(Rank.`5`, File.e).canMoveTo)
    }

    @Test
    fun `GIVEN promotion move WHEN onSquareClicked THEN promotion chooser is shown`() = runTest {
        // Given
        val underTest = buildVm(buildPromotionPuzzle())
        underTest.onSquareClicked("a7".loc())

        // When
        underTest.onSquareClicked("a8".loc())

        // Then
        val playingState = underTest.uiState.value as PuzzleStreakUiState.Playing
        assertNotNull(playingState.promotion)
        val promotion = playingState.promotion!!
        assertTrue(promotion.showChooser)
        assertEquals(Locus(File.a, Rank.`8`), promotion.at)
    }

    @Test
    fun `GIVEN promotion wins WHEN promotion selected THEN move to next puzzle`() = runTest {
        // Given
        val underTest = buildVm(buildPromotionPuzzle())
        val nextPuzzle = buildStandardPuzzle(rating = 450)
        getStreakPuzzle.enqueue(GetStreakPuzzle.Data(nextPuzzle, currentStreakCount = 1))
        underTest.onSquareClicked("a7".loc())
        underTest.onSquareClicked("a8".loc())

        // When
        underTest.onPromote(Piece.Queen)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val playingState = underTest.uiState.value as PuzzleStreakUiState.Playing
        assertEquals(1, playingState.streakCount)
        assertEquals(450, playingState.data.rating)
    }

    @Test
    fun `GIVEN puzzle loaded WHEN onHintRequested THEN selection matches hint`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())

        // When
        underTest.onHintRequested()

        // Then
        val playingState = underTest.uiState.value as PuzzleStreakUiState.Playing
        val selectedSquare = playingState.data.boardData.at("e7".loc())
        assertEquals(true, selectedSquare.piece?.isSelected)
        assertTrue(playingState.data.boardData.at("e5".loc()).canMoveTo)
    }

    @Test
    fun `GIVEN puzzle completed successfully WHEN move played THEN streak increments and next puzzle loads`() = runTest {
        // Given
        val underTest = buildVm(buildOneMoveWinPuzzle())
        val nextPuzzle = buildStandardPuzzle(rating = 450)
        getStreakPuzzle.enqueue(GetStreakPuzzle.Data(nextPuzzle, currentStreakCount = 1))

        // When - play the winning move
        underTest.onSquareClicked("e8".loc())
        underTest.onSquareClicked("e1".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val playingState = underTest.uiState.value as PuzzleStreakUiState.Playing
        assertEquals(1, playingState.streakCount)
        assertEquals(450, playingState.data.rating)
    }

    @Test
    fun `GIVEN puzzle failed WHEN wrong move played THEN streak ends`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())
        onStreakComplete.streakCount = 0

        // When - play wrong move (not e7e5)
        underTest.onSquareClicked("d7".loc())
        underTest.onSquareClicked("d5".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val endedState = underTest.uiState.value as PuzzleStreakUiState.StreakEnded
        assertEquals(0, endedState.finalStreakCount)
        assertTrue(endedState.showSummary)
        assertTrue(onStreakComplete.wasCalled)
    }

    @Test
    fun `GIVEN puzzle failed with new high score WHEN move played THEN isNewHighScore is true`() = runTest {
        // Given
        onStreakComplete.returnNewHighScore = true
        val underTest = buildVm(buildStandardPuzzle(), currentStreakCount = 50)

        // When - play wrong move
        underTest.onSquareClicked("d7".loc())
        underTest.onSquareClicked("d5".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val endedState = underTest.uiState.value as PuzzleStreakUiState.StreakEnded
        assertTrue(endedState.isNewHighScore)
    }

    @Test
    fun `GIVEN streak ended WHEN onNewStreak THEN loads new puzzle with count 0`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle(), currentStreakCount = 10)
        val newPuzzle = buildStandardPuzzle(rating = 400)
        getStreakPuzzle.enqueue(GetStreakPuzzle.Data(newPuzzle, currentStreakCount = 0))

        // End the streak
        underTest.onSquareClicked("d7".loc())
        underTest.onSquareClicked("d5".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        underTest.onNewStreak()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val playingState = underTest.uiState.value as PuzzleStreakUiState.Playing
        assertEquals(0, playingState.streakCount)
    }

    @Test
    fun `GIVEN streak ended with summary shown WHEN onDismissSummary THEN hides summary`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())
        underTest.onSquareClicked("d7".loc())
        underTest.onSquareClicked("d5".loc())
        testDispatcher.scheduler.advanceUntilIdle()

        val endedStateBefore = underTest.uiState.value as PuzzleStreakUiState.StreakEnded
        assertTrue(endedStateBefore.showSummary)

        // When
        underTest.onDismissSummary()

        // Then
        val endedStateAfter = underTest.uiState.value as PuzzleStreakUiState.StreakEnded
        assertFalse(endedStateAfter.showSummary)
    }

    private fun buildVm(
        withPuzzle: Puzzle,
        currentStreakCount: Int = 0
    ): PuzzleStreakViewModel {
        getStreakPuzzle.enqueue(GetStreakPuzzle.Data(withPuzzle, currentStreakCount = currentStreakCount))
        val underTest = PuzzleStreakViewModel(
            getStreakPuzzle = getStreakPuzzle,
            onStreakPuzzleComplete = onStreakPuzzleComplete,
            onStreakComplete = onStreakComplete,
            puzzleInteractor = RealGameFactory().puzzleInteractor()
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

    private fun buildPromotionPuzzle(): Puzzle = RealGameFactory().builder()
        .withId(PROMOTION_ID)
        .withRating(DEFAULT_RATING)
        .withTurn(Side.BLACK)
        .withPiece(Piece.King, Side.WHITE, "e1".loc())
        .withPiece(Piece.King, Side.BLACK, "e8".loc())
        .withPiece(Piece.Pawn, Side.WHITE, "a7".loc())
        .withMoves(listOf("e8e7", "a7a8q", "e7e6"))
        .buildPuzzle()

    private fun buildOneMoveWinPuzzle(): Puzzle = RealGameFactory().builder()
        .withId(ONE_MOVE_WIN_ID)
        .withRating(DEFAULT_RATING)
        .withTurn(Side.BLACK)
        .withPiece(Piece.King, Side.WHITE, "g3".loc())
        .withPiece(Piece.King, Side.BLACK, "h1".loc())
        .withPiece(Piece.Queen, Side.WHITE, "e8".loc())
        .withMoves(listOf("h1g1", "e8e1"))  // Black moves, white delivers checkmate
        .buildPuzzle()

    private companion object {
        private const val DEFAULT_RATING: Int = 400
        private const val PROMOTION_ID: Int = 42
        private const val ONE_MOVE_WIN_ID: Int = 88
    }
}

private class FakeGetStreakPuzzle : GetStreakPuzzle {
    private val results: ArrayDeque<GetStreakPuzzle.Data> = ArrayDeque()
    private var exception: Exception? = null

    fun enqueue(data: GetStreakPuzzle.Data) {
        results.addLast(data)
    }

    fun withFailure(exception: Exception) = apply {
        this.exception = exception
    }

    override suspend fun invoke(): GetStreakPuzzle.Data {
        exception?.let { throw it }
        return results.removeFirst()
    }
}

private class FakeOnStreakPuzzleComplete : OnStreakPuzzleComplete {
    override suspend fun invoke() {}
}

private class FakeOnStreakComplete : OnStreakComplete {
    var wasCalled: Boolean = false
        private set
    var returnNewHighScore: Boolean = false
    var streakCount = 10

    override suspend fun invoke(): OnStreakComplete.StreakCompleteResult {
        wasCalled = true
        return OnStreakComplete.StreakCompleteResult(returnNewHighScore, streakCount)
    }
}
