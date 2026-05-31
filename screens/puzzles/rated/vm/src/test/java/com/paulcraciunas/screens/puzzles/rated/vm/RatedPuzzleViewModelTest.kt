package com.paulcraciunas.screens.puzzles.rated.vm

import com.paulcraciunas.domain.api.general.EloResult
import com.paulcraciunas.domain.api.general.FakeTimer
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.domain.api.puzzles.OnPuzzleComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleCompletionResult
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
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
internal class RatedPuzzleViewModelTest {
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val getRatedPuzzle = FakeGetRatedPuzzle()
    private val onPuzzleComplete = FakeOnPuzzleComplete()
    private val timer = FakeTimer()
    private val appSettingsRepository = FakeAppSettingsRepository()

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
        // Given
        val withPuzzle = buildStandardPuzzle()

        // When
        val underTest = buildVm(withPuzzle)

        // Then
        assertTrue(underTest.uiState.value is RatedPuzzleUiState.Playing)
        (underTest.uiState.value as RatedPuzzleUiState.Playing).apply {
            assertEquals(DEFAULT_RATING, data.rating)
            assertEquals(Side.BLACK, data.player)
            assertTrue(hintEnabled)
            assertFalse(showAbandonDialog)
            assertNull(data.promotion)
        }
        assertTrue(timer.isRunning())
    }

    @Test
    fun `GIVEN getRatedPuzzle throws WHEN viewModel initialized THEN uiState is failed`() = runTest {
        // Given
        getRatedPuzzle.withFailure(RuntimeException("boom"))

        // When
        val underTest = buildVm(buildStandardPuzzle())

        // Then
        assertTrue(underTest.uiState.value is RatedPuzzleUiState.Failed)
    }

    @Test
    fun `GIVEN no selection WHEN onSquareClicked THEN selection and moves are marked`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())

        // When
        underTest.onSquareClicked(Locus.e7)
        testDispatcher.scheduler.runCurrent()

        // Then
        val playingState = underTest.uiState.value as RatedPuzzleUiState.Playing
        val selectedSquare = playingState.data.boardData.at(Locus.e7)
        assertEquals(true, selectedSquare.piece?.isSelected)
        assertTrue(playingState.data.boardData.at(Locus.e5).canMoveTo)
    }

    @Test
    fun `GIVEN selected square WHEN onSquareClicked invalid target THEN selection is cleared`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())
        underTest.onSquareClicked(Locus.e7)

        // When
        underTest.onSquareClicked(Locus.e4)

        // Then
        val playingState = underTest.uiState.value as RatedPuzzleUiState.Playing
        val selectedSquare = playingState.data.boardData.at(Locus.e7)
        assertEquals(false, selectedSquare.piece?.isSelected)
        assertFalse(playingState.data.boardData.at(Locus.e5).canMoveTo)
    }

    @Test
    fun `GIVEN promotion move WHEN onSquareClicked THEN promotion chooser is shown`() = runTest {
        // Given
        appSettingsRepository.updateAutoPromote(false)
        val underTest = buildVm(buildPromotionPuzzle())
        underTest.onSquareClicked(Locus.a7)

        // When
        underTest.onSquareClicked(Locus.a8)
        testDispatcher.scheduler.runCurrent()

        // Then
        val playingState = underTest.uiState.value as RatedPuzzleUiState.Playing
        assertNotNull(playingState.data.promotion)
        val promotion = playingState.data.promotion!!
        assertTrue(promotion.showChooser)
        assertEquals(Locus.a8, promotion.at)
    }

    @Test
    fun `GIVEN promotion chooser WHEN onPromote THEN pawn is promoted and state updated`() = runTest {
        // Given
        appSettingsRepository.updateAutoPromote(false)
        val underTest = buildVm(buildPromotionPuzzle())
        underTest.onSquareClicked(Locus.a7)
        underTest.onSquareClicked(Locus.a8)

        // When
        underTest.onPromote(Piece.Queen)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val finishedState = underTest.uiState.value as RatedPuzzleUiState.Finished
        val promotedSquare = finishedState.data.boardData.at(Locus.a8)
        assertEquals(Piece.Queen, promotedSquare.piece?.piece?.piece)
        assertEquals(Side.WHITE, promotedSquare.piece?.piece?.side)
    }

    @Test
    fun `GIVEN puzzle loaded WHEN onHintRequested THEN selection matches hint`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())

        // When
        underTest.onHintRequested()
        testDispatcher.scheduler.runCurrent()

        // Then
        val playingState = underTest.uiState.value as RatedPuzzleUiState.Playing
        val selectedSquare = playingState.data.boardData.at(Locus.e7)
        assertEquals(true, selectedSquare.piece?.isSelected)
        assertTrue(playingState.data.boardData.at(Locus.e5).canMoveTo)
    }

    @Test
    fun `GIVEN playing state WHEN onAbandon THEN abandon dialog is shown`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())

        // When
        underTest.onAbandon()

        // Then
        val playingState = underTest.uiState.value as RatedPuzzleUiState.Playing
        assertTrue(playingState.showAbandonDialog)
    }

    @Test
    fun `GIVEN abandon dialog shown WHEN onAbandonDismissed THEN dialog is hidden`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())
        underTest.onAbandon()

        // When
        underTest.onAbandonDismissed()

        // Then
        val playingState: RatedPuzzleUiState.Playing = underTest.uiState.value as RatedPuzzleUiState.Playing
        assertFalse(playingState.showAbandonDialog)
    }

    @Test
    fun `GIVEN playing state WHEN onAbandonConfirmed THEN puzzle is finished and result logged`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())
        val elapsedMillis = 500L

        // When
        underTest.onAbandonConfirmed()
        timer.advanceTimeBy(elapsedMillis)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val finishedState = underTest.uiState.value as RatedPuzzleUiState.Finished
        assertFalse(finishedState.success)
        assertEquals(-10, finishedState.ratingChange)

        assertNotNull(onPuzzleComplete.lastResult)
        val loggedResult = onPuzzleComplete.lastResult!!
        assertEquals(DEFAULT_RATING, loggedResult.puzzleRating)
        assertEquals(false, loggedResult.wasSuccessful)
        assertEquals(-10, loggedResult.ratingChange)
        assertEquals(timer.elapsed(), loggedResult.timeSpentMillis)
    }

    @Test
    fun `GIVEN playing state WHEN onNavigateBackPressed THEN returns true and shows abandon dialog`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())

        // When
        val handled = underTest.onNavigateBackPressed()

        // Then
        assertTrue(handled)
        val playingState = underTest.uiState.value as RatedPuzzleUiState.Playing
        assertTrue(playingState.showAbandonDialog)
    }

    @Test
    fun `GIVEN failed state WHEN onNavigateBackPressed THEN returns false`() = runTest {
        // Given
        getRatedPuzzle.withFailure(RuntimeException("boom"))
        val underTest = buildVm(buildStandardPuzzle())

        // When
        val handled = underTest.onNavigateBackPressed()

        // Then
        assertFalse(handled)
    }

    @Test
    fun `GIVEN multiple puzzles WHEN onNextPuzzle THEN loads next puzzle`() = runTest {
        // Given
        getRatedPuzzle.enqueue(GetRatedPuzzle.Data(buildStandardPuzzle(), ratingChange = EloResult(20, -10)))
        val underTest = buildVm(buildStandardPuzzle(rating = 1300))

        // When
        underTest.onNextPuzzle()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val playingState: RatedPuzzleUiState.Playing = underTest.uiState.value as RatedPuzzleUiState.Playing
        assertEquals(1300, playingState.data.rating)
    }

    @Test
    fun `GIVEN playing state WHEN onStop THEN timer is paused`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())
        assertTrue(timer.isRunning())

        // When
        underTest.onStop()

        // Then
        assertFalse(timer.isRunning())
    }

    @Test
    fun `GIVEN app backgrounded WHEN onStop then onStart THEN timer pauses and resumes`() = runTest {
        // Given
        val underTest = buildVm(buildStandardPuzzle())

        // When - simulate app going to background and coming back
        underTest.onStop()
        underTest.onStart()

        // Then
        assertTrue(timer.isRunning())
    }

    private fun buildVm(withPuzzle: Puzzle): RatedPuzzleViewModel {
        getRatedPuzzle.enqueue(GetRatedPuzzle.Data(withPuzzle, ratingChange = EloResult(20, -10)))
        val underTest = RatedPuzzleViewModel(
            getRatedPuzzle = getRatedPuzzle,
            onPuzzleComplete = onPuzzleComplete,
            appSettingsRepository = appSettingsRepository,
            timer = timer,
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
        .withRating(DEFAULT_RATING)
        .withTurn(Side.BLACK)
        .withPiece(Piece.King, Side.WHITE, Locus.e1)
        .withPiece(Piece.King, Side.BLACK, Locus.e8)
        .withPiece(Piece.Pawn, Side.WHITE, Locus.a7)
        .withMoves(listOf("e8e7", "a7a8q", "e7e6"))
        .buildPuzzle()

    private companion object {
        private const val DEFAULT_RATING: Int = 1200
    }
}

private class FakeGetRatedPuzzle : GetRatedPuzzle {
    private val results: ArrayDeque<GetRatedPuzzle.Data> = ArrayDeque()
    private var exception: Exception? = null

    fun enqueue(data: GetRatedPuzzle.Data) {
        results.addLast(data)
    }

    fun withFailure(exception: Exception) = apply {
        this.exception = exception
    }

    override suspend fun invoke(): GetRatedPuzzle.Data {
        exception?.let { throw it }
        return results.removeFirst()
    }
}

private class FakeOnPuzzleComplete : OnPuzzleComplete {
    var lastResult: PuzzleCompletionResult? = null
        private set

    override suspend fun invoke(completionResult: PuzzleCompletionResult) {
        lastResult = completionResult
    }
}
