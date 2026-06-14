package com.paulcraciunas.screens.puzzles.streak.vm

import com.paulcraciunas.domain.api.achievements.FakeAchievementNotificationManager
import com.paulcraciunas.domain.api.general.FakeTimer
import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.impl.achievements.UpdateAchievementProgressImpl
import com.paulcraciunas.domain.impl.puzzles.OnStreakCompleteImpl
import com.paulcraciunas.domain.impl.puzzles.OnStreakPuzzleCompleteImpl
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PuzzleStreakViewModelTest {
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private val getStreakPuzzle = QueuedGetStreakPuzzle()
    private val timer = FakeTimer()
    private val appSettingsRepository = FakeAppSettingsRepository.default()
    private val userRepository = FakeUserRepository()
    private val onStreakComplete = OnStreakCompleteImpl(
        userRepository = userRepository,
        updateAchievementProgress = UpdateAchievementProgressImpl(FakeAchievementNotificationManager()),
        dispatcher = testDispatcher,
    )
    private val onStreakPuzzleComplete = OnStreakPuzzleCompleteImpl(
        userRepository = userRepository,
    )

    private lateinit var underTest: PuzzleStreakViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appSettingsRepository.setAppSettings(
            FakeAppSettingsRepository.defaultSettings().copy(enableAnimations = false)
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    internal inner class Initialization {
        @Test
        fun `GIVEN puzzle loaded WHEN viewModel initialized THEN uiState is Playing`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            val state = underTest.uiState.value
            assertTrue(state is PuzzleStreakUiState.Playing)
            val playing = state as PuzzleStreakUiState.Playing
            assertEquals(DEFAULT_RATING, playing.data.rating)
            assertEquals(Side.BLACK, playing.data.player)
            assertEquals(0, playing.streakCount)
        }

        @Test
        fun `GIVEN getStreakPuzzle throws WHEN viewModel initialized THEN uiState is Failed`() = runTest(testDispatcher) {
            getStreakPuzzle.withFailure(RuntimeException("boom"))

            buildVm(skipEnqueue = true)

            assertTrue(underTest.uiState.value is PuzzleStreakUiState.Failed)
        }

        @Test
        fun `GIVEN existing streak WHEN viewModel initialized THEN shows current streak count`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle(), currentStreakCount = 5)

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertEquals(5, playing.streakCount)
        }

        @Test
        fun `GIVEN hints configured WHEN initialized THEN hintEnabled is true`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertTrue(playing.hintEnabled)
        }
    }

    @Nested
    internal inner class PlayerMoves {
        @Test
        fun `GIVEN no selection WHEN onSquareClicked THEN selection and moves are marked`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            val selectedSquare = playing.data.boardData.at(Locus.e7)
            assertEquals(true, selectedSquare.piece?.isSelected)
            assertTrue(playing.data.boardData.at(Locus.e5).canMoveTo)
        }

        @Test
        fun `GIVEN selected square WHEN onSquareClicked invalid target THEN selection is cleared`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()
            underTest.onSquareClicked(Locus.e4)
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            val selectedSquare = playing.data.boardData.at(Locus.e7)
            assertEquals(false, selectedSquare.piece?.isSelected)
            assertFalse(playing.data.boardData.at(Locus.e5).canMoveTo)
        }

        @Test
        fun `GIVEN correct move WHEN played THEN opponent responds and puzzle continues`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            makeMove(from = Locus.e7, to = Locus.e5)

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertNotNull(playing.data.boardData.at(Locus.f3).piece)
        }

        @Test
        fun `GIVEN promotion move WHEN onSquareClicked THEN promotion chooser is shown`() = runTest(testDispatcher) {
            appSettingsRepository.setAppSettings(
                appSettingsRepository.getCurrentSettings().copy(autoPromote = false)
            )
            buildVm(buildPromotionPuzzle())

            underTest.onSquareClicked(Locus.a2)
            advanceUntilIdle()
            underTest.onSquareClicked(Locus.a1)
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertNotNull(playing.data.promotion)
            val promotion = playing.data.promotion!!
            assertTrue(promotion.showChooser)
            assertEquals(Locus.a1, promotion.at)
        }

        @Test
        fun `GIVEN promotion wins WHEN promotion selected THEN moves to next puzzle`() = runTest(testDispatcher) {
            appSettingsRepository.setAppSettings(
                appSettingsRepository.getCurrentSettings().copy(
                    autoPromote = false,
                    autoNextPuzzle = true,
                )
            )
            buildVm(buildPromotionPuzzle())
            getStreakPuzzle.enqueue(GetStreakPuzzle.Data(buildStandardPuzzle(rating = 450), currentStreakCount = 1))

            underTest.onSquareClicked(Locus.a2)
            advanceUntilIdle()
            underTest.onSquareClicked(Locus.a1)
            advanceUntilIdle()
            underTest.onPromote(Piece.Queen)
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertEquals(1, playing.streakCount)
            assertEquals(450, playing.data.rating)
        }
    }

    @Nested
    internal inner class StreakProgression {
        @Test
        fun `GIVEN puzzle completed WHEN autoNext enabled THEN streak increments and next puzzle loads`() = runTest(testDispatcher) {
            appSettingsRepository.setAppSettings(
                appSettingsRepository.getCurrentSettings().copy(autoNextPuzzle = true)
            )
            buildVm(buildOneMoveWinPuzzle())
            getStreakPuzzle.enqueue(GetStreakPuzzle.Data(buildStandardPuzzle(rating = 450), currentStreakCount = 1))

            makeMove(from = Locus.e8, to = Locus.e1)

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertEquals(1, playing.streakCount)
            assertEquals(450, playing.data.rating)
            assertFalse(playing.isAwaitingNextPuzzle)
        }

        @Test
        fun `GIVEN puzzle completed WHEN autoNext disabled THEN isAwaitingNextPuzzle is true`() = runTest(testDispatcher) {
            appSettingsRepository.setAppSettings(
                appSettingsRepository.getCurrentSettings().copy(autoNextPuzzle = false)
            )
            buildVm(buildOneMoveWinPuzzle())

            makeMove(from = Locus.e8, to = Locus.e1)

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertTrue(playing.isAwaitingNextPuzzle)
        }

        @Test
        fun `GIVEN isAwaitingNextPuzzle WHEN onNextPuzzle THEN loads next puzzle`() = runTest(testDispatcher) {
            appSettingsRepository.setAppSettings(
                appSettingsRepository.getCurrentSettings().copy(autoNextPuzzle = false)
            )
            buildVm(buildOneMoveWinPuzzle())
            getStreakPuzzle.enqueue(GetStreakPuzzle.Data(buildStandardPuzzle(rating = 500), currentStreakCount = 1))

            makeMove(from = Locus.e8, to = Locus.e1)
            assertTrue((underTest.uiState.value as PuzzleStreakUiState.Playing).isAwaitingNextPuzzle)

            underTest.onNextPuzzle()
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertEquals(1, playing.streakCount)
            assertEquals(500, playing.data.rating)
            assertFalse(playing.isAwaitingNextPuzzle)
        }

        @Test
        fun `GIVEN isAwaitingNextPuzzle WHEN onSquareClicked THEN interaction is blocked`() = runTest(testDispatcher) {
            appSettingsRepository.setAppSettings(
                appSettingsRepository.getCurrentSettings().copy(autoNextPuzzle = false)
            )
            buildVm(buildOneMoveWinPuzzle())

            makeMove(from = Locus.e8, to = Locus.e1)
            assertTrue((underTest.uiState.value as PuzzleStreakUiState.Playing).isAwaitingNextPuzzle)

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()

            assertTrue((underTest.uiState.value as PuzzleStreakUiState.Playing).isAwaitingNextPuzzle)
        }

        @Test
        fun `GIVEN puzzle completed WHEN onSessionComplete THEN user streak count incremented`() = runTest(testDispatcher) {
            appSettingsRepository.setAppSettings(
                appSettingsRepository.getCurrentSettings().copy(autoNextPuzzle = true)
            )
            buildVm(buildOneMoveWinPuzzle())
            getStreakPuzzle.enqueue(GetStreakPuzzle.Data(buildStandardPuzzle(), currentStreakCount = 1))

            makeMove(from = Locus.e8, to = Locus.e1)

            val user = userRepository.get()
            assertEquals(1, user.ratings.puzzleStreak.currentCount)
        }
    }

    @Nested
    internal inner class StreakEnd {
        @Test
        fun `GIVEN puzzle failed WHEN wrong move played THEN streak ends`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            makeMove(from = Locus.d7, to = Locus.d5)

            val ended = underTest.uiState.value as PuzzleStreakUiState.StreakEnded
            assertEquals(0, ended.streakCount)
            assertTrue(ended.showSummary)
        }

        @Test
        fun `GIVEN puzzle failed WHEN streak ends THEN onStreakComplete persists data`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            makeMove(from = Locus.d7, to = Locus.d5)

            val user = userRepository.get()
            assertEquals(0, user.ratings.puzzleStreak.currentCount)
            assertEquals(1, user.statistics.streakSessions)
        }

        @Test
        fun `GIVEN streak count exceeds high score WHEN puzzle fails THEN isNewHighScore is true`() = runTest(testDispatcher) {
            userRepository.local.saveUser(User(highScores = User.HighScores(puzzleStreak = 3)))
            buildVm(buildStandardPuzzle(), currentStreakCount = 5)

            makeMove(from = Locus.d7, to = Locus.d5)

            val ended = underTest.uiState.value as PuzzleStreakUiState.StreakEnded
            assertTrue(ended.isNewHighScore)
        }

        @Test
        fun `GIVEN streak count does not exceed high score WHEN puzzle fails THEN isNewHighScore is false`() = runTest(testDispatcher) {
            userRepository.local.saveUser(User(highScores = User.HighScores(puzzleStreak = 10)))
            buildVm(buildStandardPuzzle(), currentStreakCount = 3)

            makeMove(from = Locus.d7, to = Locus.d5)

            val ended = underTest.uiState.value as PuzzleStreakUiState.StreakEnded
            assertFalse(ended.isNewHighScore)
        }

        @Test
        fun `GIVEN streak ended WHEN onDismissSummary THEN hides summary`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            makeMove(from = Locus.d7, to = Locus.d5)
            assertTrue((underTest.uiState.value as PuzzleStreakUiState.StreakEnded).showSummary)

            underTest.onDismissSummary()
            advanceUntilIdle()

            assertFalse((underTest.uiState.value as PuzzleStreakUiState.StreakEnded).showSummary)
        }

        @Test
        fun `GIVEN streak ended WHEN onNewStreak THEN loads new puzzle`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle(), currentStreakCount = 5)

            makeMove(from = Locus.d7, to = Locus.d5)
            assertTrue(underTest.uiState.value is PuzzleStreakUiState.StreakEnded)

            getStreakPuzzle.enqueue(GetStreakPuzzle.Data(buildStandardPuzzle(rating = 400), currentStreakCount = 0))
            underTest.onNewStreak()
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertEquals(0, playing.streakCount)
            assertEquals(400, playing.data.rating)
        }
    }

    @Nested
    internal inner class Hints {
        @Test
        fun `GIVEN puzzle loaded WHEN onHintRequested THEN selection matches hint`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            underTest.onHintRequested()
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            val selectedSquare = playing.data.boardData.at(Locus.e7)
            assertEquals(true, selectedSquare.piece?.isSelected)
            assertTrue(playing.data.boardData.at(Locus.e5).canMoveTo)
        }

        @Test
        fun `GIVEN hint used WHEN single hint mode THEN hintEnabled becomes false`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())
            assertTrue((underTest.uiState.value as PuzzleStreakUiState.Playing).hintEnabled)

            underTest.onHintRequested()
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertFalse(playing.hintEnabled)
        }
    }

    @Nested
    internal inner class Abandon {
        @Test
        fun `GIVEN playing WHEN onAbandon THEN showAbandonDialog is true`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()
            underTest.onAbandon()
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertTrue(playing.showAbandonDialog)
        }

        @Test
        fun `GIVEN abandon dialog WHEN onAbandonDismissed THEN dialog hidden`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()
            underTest.onAbandon()
            advanceUntilIdle()
            underTest.onAbandonDismissed()
            advanceUntilIdle()

            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertFalse(playing.showAbandonDialog)
        }

        @Test
        fun `GIVEN abandon dialog WHEN onAbandonConfirmed THEN streak ends`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()
            underTest.onAbandonConfirmed()
            advanceUntilIdle()

            assertTrue(underTest.uiState.value is PuzzleStreakUiState.StreakEnded)
        }

        @Test
        fun `GIVEN playing WHEN onNavigateBackPressed THEN shows abandon dialog and returns true`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()

            val consumed = underTest.onNavigateBackPressed()
            advanceUntilIdle()

            assertTrue(consumed)
            val playing = underTest.uiState.value as PuzzleStreakUiState.Playing
            assertTrue(playing.showAbandonDialog)
        }

        @Test
        fun `GIVEN streak ended WHEN onNavigateBackPressed THEN returns false`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            makeMove(from = Locus.d7, to = Locus.d5)
            assertTrue(underTest.uiState.value is PuzzleStreakUiState.StreakEnded)

            val consumed = underTest.onNavigateBackPressed()

            assertFalse(consumed)
        }
    }

    @Nested
    internal inner class Timer {
        @Test
        fun `GIVEN viewModel WHEN onNewStreak THEN timer starts`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            assertTrue(timer.isRunning())
        }

        @Test
        fun `GIVEN playing WHEN onStop THEN timer pauses`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            underTest.onStop()

            assertFalse(timer.isRunning())
        }

        @Test
        fun `GIVEN paused WHEN onStart THEN timer resumes`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())

            underTest.onStop()
            assertFalse(timer.isRunning())

            underTest.onStart()

            assertTrue(timer.isRunning())
        }

        @Test
        fun `GIVEN streak ended WHEN onStreakComplete THEN elapsed time is persisted`() = runTest(testDispatcher) {
            buildVm(buildStandardPuzzle())
            timer.advanceTimeBy(5000L)

            makeMove(from = Locus.d7, to = Locus.d5)

            val history = userRepository.get().history
            assertTrue(history.isNotEmpty())
            val streakData = history.first().data as User.HistoryItem.HistoryItemData.PuzzleStreakData
            assertEquals(5000L, streakData.timeSpent)
        }
    }

    private fun TestScope.buildVm(
        puzzle: Puzzle = buildStandardPuzzle(),
        currentStreakCount: Int = 0,
        skipEnqueue: Boolean = false,
    ) {
        if (!skipEnqueue) {
            getStreakPuzzle.enqueue(GetStreakPuzzle.Data(puzzle, currentStreakCount = currentStreakCount))
        }
        underTest = PuzzleStreakViewModel(
            timer = timer,
            userRepository = userRepository,
            appSettingsRepository = appSettingsRepository,
            defaultDispatcher = testDispatcher,
            getStreakPuzzle = getStreakPuzzle,
            onStreakPuzzleComplete = onStreakPuzzleComplete,
            onStreakComplete = onStreakComplete,
            sounds = SoundCoordinator(),
        )
        backgroundScope.launch {
            underTest.uiState.collect {}
        }
        advanceUntilIdle()
    }

    private fun TestScope.makeMove(from: Locus, to: Locus) {
        underTest.onSquareClicked(from)
        advanceUntilIdle()
        underTest.onSquareClicked(to)
        advanceUntilIdle()
    }

    private fun buildStandardPuzzle(
        rating: Int = DEFAULT_RATING,
        moves: List<String> = listOf("e2e4", "e7e5", "g1f3", "b8c6"),
    ): Puzzle = RealGameFactory().builder()
        .withDefaultBoard()
        .withRating(rating)
        .withId(nextId++)
        .withMoves(moves)
        .buildPuzzle()

    private fun buildPromotionPuzzle(): Puzzle = RealGameFactory().builder()
        .withId(nextId++)
        .withRating(DEFAULT_RATING)
        .withTurn(Side.WHITE)
        .withPiece(Piece.King, Side.WHITE, Locus.e1)
        .withPiece(Piece.Pawn, Side.WHITE, Locus.g2)
        .withPiece(Piece.King, Side.BLACK, Locus.e8)
        .withPiece(Piece.Pawn, Side.BLACK, Locus.a2)
        .withMoves(listOf("g2g3", "a2a1"))
        .buildPuzzle()

    private fun buildOneMoveWinPuzzle(): Puzzle = RealGameFactory().builder()
        .withId(nextId++)
        .withRating(DEFAULT_RATING)
        .withTurn(Side.BLACK)
        .withPiece(Piece.King, Side.WHITE, Locus.g3)
        .withPiece(Piece.King, Side.BLACK, Locus.h1)
        .withPiece(Piece.Queen, Side.WHITE, Locus.e8)
        .withMoves(listOf("h1g1", "e8e1"))
        .buildPuzzle()

    private var nextId = 1

    private companion object {
        const val DEFAULT_RATING = 400
    }
}

private class QueuedGetStreakPuzzle : GetStreakPuzzle {
    private val channel = Channel<GetStreakPuzzle.Data>(capacity = Channel.UNLIMITED)
    private var exception: Exception? = null

    fun enqueue(data: GetStreakPuzzle.Data) {
        channel.trySend(data)
    }

    fun withFailure(exception: Exception) {
        this.exception = exception
    }

    override suspend fun invoke(): GetStreakPuzzle.Data {
        exception?.let { throw it }
        return channel.receive()
    }
}
