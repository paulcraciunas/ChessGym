package com.paulcraciunas.screens.puzzles.rated.vm

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.general.EloResult
import com.paulcraciunas.domain.api.general.FakeTimer
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.domain.impl.puzzles.OnPuzzleCompleteImpl
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class RatedPuzzleViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val gameFactory = RealGameFactory()

    private val getRatedPuzzle = QueuedGetRatedPuzzle()
    private val timer = FakeTimer()
    private val appSettingsRepository = FakeAppSettingsRepository.default()
    private val userRepository = FakeUserRepository()
    private val noOpAchievements = object : UpdateAchievementProgress {
        override suspend fun invoke(user: User): User = user
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appSettingsRepository.setAppSettings(
            appSettingsRepository.getCurrentSettings().copy(enableAnimations = false)
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    internal inner class Initialization {
        @Test
        fun `GIVEN puzzle loaded WHEN viewModel initialized THEN uiState is Playing`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertEquals(DEFAULT_RATING, state.rating)
            assertEquals(Side.BLACK, state.data.player)
            assertTrue(state.hintEnabled)
            assertFalse(state.showAbandonDialog)
            assertNull(state.data.promotion)
        }

        @Test
        fun `GIVEN puzzle loaded WHEN viewModel initialized THEN timer is started`() = runTest {
            buildVm(buildStandardPuzzle())

            assertTrue(timer.isRunning())
        }

        @Test
        fun `GIVEN getRatedPuzzle throws WHEN viewModel initialized THEN uiState is Failed`() = runTest {
            getRatedPuzzle.withFailure(RuntimeException("boom"))

            val underTest = buildVm()

            assertEquals(RatedPuzzleUiState.Failed, underTest.uiState.value)
        }

        @Test
        fun `GIVEN puzzle loaded WHEN viewModel initialized THEN opponent first move is played`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertNotNull(state.data.boardData.at(Locus.e4).piece)
        }
    }

    @Nested
    internal inner class PlayerInteraction {
        @Test
        fun `GIVEN no selection WHEN onSquareClicked THEN piece is selected with valid moves`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            underTest.onSquareClicked(Locus.e7)
            testDispatcher.scheduler.runCurrent()

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertEquals(true, state.data.boardData.at(Locus.e7).piece?.isSelected)
            assertTrue(state.data.boardData.at(Locus.e5).canMoveTo)
        }

        @Test
        fun `GIVEN selected square WHEN clicking invalid target THEN selection is cleared`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            underTest.onSquareClicked(Locus.e7)
            testDispatcher.scheduler.runCurrent()
            underTest.onSquareClicked(Locus.e4)
            testDispatcher.scheduler.runCurrent()

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertEquals(false, state.data.boardData.at(Locus.e7).piece?.isSelected)
            assertFalse(state.data.boardData.at(Locus.e5).canMoveTo)
        }

        @Test
        fun `GIVEN correct move WHEN played THEN opponent responds and game continues`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            makeMove(underTest, from = Locus.e7, to = Locus.e5)

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertNotNull(state.data.boardData.at(Locus.f3).piece)
        }

        @Test
        fun `GIVEN all correct moves WHEN puzzle solved THEN state is Finished with success`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            makeMove(underTest, from = Locus.e7, to = Locus.e5)
            makeMove(underTest, from = Locus.b8, to = Locus.c6)

            val state = underTest.uiState.value as RatedPuzzleUiState.Finished
            assertTrue(state.success)
            assertEquals(DEFAULT_RATING, state.rating)
            assertEquals(DEFAULT_GAIN, state.ratingChange)
        }

        @Test
        fun `GIVEN wrong move WHEN played THEN state is Finished with failure`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            makeMove(underTest, from = Locus.d7, to = Locus.d5)

            val state = underTest.uiState.value as RatedPuzzleUiState.Finished
            assertFalse(state.success)
            assertEquals(DEFAULT_LOSS, state.ratingChange)
        }
    }

    @Nested
    internal inner class Promotion {
        @Test
        fun `GIVEN promotion move WHEN autoPromote off THEN chooser is shown`() = runTest {
            appSettingsRepository.updateAutoPromote(false)
            val underTest = buildVm(buildPromotionPuzzle())

            makeMove(underTest, from = Locus.a2, to = Locus.a1)

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertNotNull(state.data.promotion)
            assertTrue(state.data.promotion!!.showChooser)
            assertEquals(Locus.a1, state.data.promotion!!.at)
        }

        @Test
        fun `GIVEN promotion chooser WHEN onPromote THEN pawn is promoted and puzzle finishes`() = runTest {
            appSettingsRepository.updateAutoPromote(false)
            val underTest = buildVm(buildPromotionPuzzle())

            makeMove(underTest, from = Locus.a2, to = Locus.a1)
            underTest.onPromote(Piece.Queen)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = underTest.uiState.value as RatedPuzzleUiState.Finished
            assertTrue(state.success)
            val promotedSquare = state.data.boardData.at(Locus.a1)
            assertEquals(Piece.Queen, promotedSquare.piece?.piece?.piece)
            assertEquals(Side.BLACK, promotedSquare.piece?.piece?.side)
        }

        @Test
        fun `GIVEN promotion move WHEN autoPromote on THEN auto-promotes to Queen`() = runTest {
            appSettingsRepository.updateAutoPromote(true)
            val underTest = buildVm(buildPromotionPuzzle())

            makeMove(underTest, from = Locus.a2, to = Locus.a1)

            val state = underTest.uiState.value as RatedPuzzleUiState.Finished
            assertTrue(state.success)
        }
    }

    @Nested
    internal inner class Hints {
        @Test
        fun `GIVEN puzzle loaded WHEN onHintRequested THEN correct piece is highlighted`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            underTest.onHintRequested()
            testDispatcher.scheduler.runCurrent()

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertEquals(true, state.data.boardData.at(Locus.e7).piece?.isSelected)
            assertTrue(state.data.boardData.at(Locus.e5).canMoveTo)
        }

        @Test
        fun `GIVEN hint used WHEN single hint mode THEN hintEnabled becomes false`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            underTest.onHintRequested()
            testDispatcher.scheduler.runCurrent()

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertFalse(state.hintEnabled)
        }
    }

    @Nested
    internal inner class Abandon {
        @Test
        fun `GIVEN playing state WHEN onAbandon THEN abandon dialog is shown`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            underTest.onAbandon()
            testDispatcher.scheduler.runCurrent()

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertTrue(state.showAbandonDialog)
        }

        @Test
        fun `GIVEN abandon dialog WHEN onAbandonDismissed THEN dialog is hidden`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            underTest.onAbandon()
            testDispatcher.scheduler.runCurrent()
            underTest.onAbandonDismissed()
            testDispatcher.scheduler.runCurrent()

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertFalse(state.showAbandonDialog)
        }

        @Test
        fun `GIVEN playing state WHEN onAbandonConfirmed THEN puzzle finishes as failure`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            underTest.onAbandonConfirmed()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = underTest.uiState.value as RatedPuzzleUiState.Finished
            assertFalse(state.success)
            assertEquals(DEFAULT_LOSS, state.ratingChange)
        }
    }

    @Nested
    internal inner class BackNavigation {
        @Test
        fun `GIVEN playing state WHEN onNavigateBackPressed THEN returns true and shows abandon`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            underTest.onSquareClicked(Locus.e7)
            testDispatcher.scheduler.runCurrent()

            val handled = underTest.onNavigateBackPressed()
            testDispatcher.scheduler.runCurrent()

            assertTrue(handled)
            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertTrue(state.showAbandonDialog)
        }

        @Test
        fun `GIVEN failed state WHEN onNavigateBackPressed THEN returns false`() = runTest {
            getRatedPuzzle.withFailure(RuntimeException("boom"))
            val underTest = buildVm()

            val handled = underTest.onNavigateBackPressed()

            assertFalse(handled)
        }
    }

    @Nested
    internal inner class NextPuzzle {
        @Test
        fun `GIVEN puzzle finished WHEN onNextPuzzle THEN loads next puzzle`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            val secondPuzzle = buildStandardPuzzle(rating = 1250)
            getRatedPuzzle.enqueue(GetRatedPuzzle.Data(secondPuzzle, EloResult(25, -15)))

            makeMove(underTest, from = Locus.e7, to = Locus.e5)
            makeMove(underTest, from = Locus.b8, to = Locus.c6)

            assertTrue(underTest.uiState.value is RatedPuzzleUiState.Finished)

            underTest.onNextPuzzle()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = underTest.uiState.value as RatedPuzzleUiState.Playing
            assertEquals(1250, state.rating)
        }
    }

    @Nested
    internal inner class TimerLifecycle {
        @Test
        fun `GIVEN playing WHEN onStop THEN timer is paused`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())
            assertTrue(timer.isRunning())

            underTest.onStop()

            assertFalse(timer.isRunning())
        }

        @Test
        fun `GIVEN timer paused WHEN onStart THEN timer resumes`() = runTest {
            val underTest = buildVm(buildStandardPuzzle())

            underTest.onStop()
            assertFalse(timer.isRunning())

            underTest.onStart()
            assertTrue(timer.isRunning())
        }
    }

    @Nested
    internal inner class CompletionTracking {
        @Test
        fun `GIVEN puzzle won WHEN completed THEN user rating is updated`() = runTest {
            assertEquals(User.DEFAULT_RATED_PUZZLE_RATING, userRepository.get().ratings.current)

            val underTest = buildVm(buildStandardPuzzle())
            makeMove(underTest, from = Locus.e7, to = Locus.e5)
            makeMove(underTest, from = Locus.b8, to = Locus.c6)

            val updatedUser = userRepository.get()
            assertEquals(User.DEFAULT_RATED_PUZZLE_RATING + DEFAULT_GAIN, updatedUser.ratings.current)
            assertEquals(1, updatedUser.statistics.puzzlesPlayed)
            assertEquals(1, updatedUser.statistics.puzzlesSolved)
        }

        @Test
        fun `GIVEN puzzle lost WHEN completed THEN user rating decreases and failed puzzle tracked`() = runTest {
            val underTest = buildVm(buildStandardPuzzle(id = 42))

            makeMove(underTest, from = Locus.d7, to = Locus.d5)

            val updatedUser = userRepository.get()
            assertEquals(User.DEFAULT_RATED_PUZZLE_RATING + DEFAULT_LOSS, updatedUser.ratings.current)
            assertEquals(1, updatedUser.statistics.puzzlesPlayed)
            assertEquals(0, updatedUser.statistics.puzzlesSolved)
            assertTrue(updatedUser.failedPuzzles.contains(42))
        }

        @Test
        fun `GIVEN puzzle abandoned WHEN completed THEN treated as loss`() = runTest {
            val underTest = buildVm(buildStandardPuzzle(id = 7))

            underTest.onAbandonConfirmed()
            testDispatcher.scheduler.advanceUntilIdle()

            val updatedUser = userRepository.get()
            assertEquals(User.DEFAULT_RATED_PUZZLE_RATING + DEFAULT_LOSS, updatedUser.ratings.current)
            assertTrue(updatedUser.failedPuzzles.contains(7))
        }
    }

    private fun TestScope.buildVm(firstPuzzle: Puzzle? = null): RatedPuzzleViewModel {
        if (firstPuzzle != null) {
            getRatedPuzzle.enqueue(GetRatedPuzzle.Data(firstPuzzle, EloResult(DEFAULT_GAIN, DEFAULT_LOSS)))
        }
        val onPuzzleComplete = OnPuzzleCompleteImpl(userRepository, noOpAchievements)
        val vm = RatedPuzzleViewModel(
            defaultDispatcher = testDispatcher,
            timer = timer,
            getRatedPuzzle = getRatedPuzzle,
            onPuzzleComplete = onPuzzleComplete,
            appSettingsRepository = appSettingsRepository,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            vm.uiState.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    private fun makeMove(vm: RatedPuzzleViewModel, from: Locus, to: Locus) {
        vm.onSquareClicked(from)
        testDispatcher.scheduler.runCurrent()
        vm.onSquareClicked(to)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun buildStandardPuzzle(
        rating: Int = DEFAULT_RATING,
        id: Int = 1,
        moves: List<String> = listOf("e2e4", "e7e5", "g1f3", "b8c6"),
    ): Puzzle = gameFactory.builder()
        .withId(id)
        .withDefaultBoard()
        .withRating(rating)
        .withMoves(moves)
        .buildPuzzle()

    private fun buildPromotionPuzzle(): Puzzle = gameFactory.builder()
        .withId(2)
        .withRating(DEFAULT_RATING)
        .withTurn(Side.WHITE)
        .withPiece(Piece.King, Side.WHITE, Locus.e1)
        .withPiece(Piece.Pawn, Side.WHITE, Locus.g2)
        .withPiece(Piece.King, Side.BLACK, Locus.e8)
        .withPiece(Piece.Pawn, Side.BLACK, Locus.a2)
        .withMoves(listOf("g2g3", "a2a1"))
        .buildPuzzle()

    private companion object {
        private const val DEFAULT_RATING = 1200
        private const val DEFAULT_GAIN = 20
        private const val DEFAULT_LOSS = -10
    }
}

private class QueuedGetRatedPuzzle : GetRatedPuzzle {
    private val channel = Channel<GetRatedPuzzle.Data>(capacity = Channel.UNLIMITED)
    private var exception: Exception? = null

    fun enqueue(data: GetRatedPuzzle.Data) {
        channel.trySend(data)
    }

    fun withFailure(exception: Exception) {
        this.exception = exception
    }

    override suspend fun invoke(): GetRatedPuzzle.Data {
        exception?.let { throw it }
        return channel.receive()
    }
}
