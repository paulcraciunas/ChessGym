package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.domain.api.achievements.FakeAchievementNotificationManager
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.FakeCountdownTimer
import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.impl.achievements.UpdateAchievementProgressImpl
import com.paulcraciunas.domain.impl.puzzles.OnPuzzleRushCompleteImpl
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.global.navigation.NavigationDispatcher
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
internal class PuzzleRushViewModelTest {
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private val appSettingsRepository = FakeAppSettingsRepository.default()
    private val countdownTimer = FakeCountdownTimer()
    private val userRepository = FakeUserRepository()
    private val navDispatcher = NavigationDispatcher()
    private val puzzleSeries = QueuedPuzzleSeries()
    private val onPuzzleRushComplete = OnPuzzleRushCompleteImpl(
        userRepository = userRepository,
        updateAchievementProgress = UpdateAchievementProgressImpl(FakeAchievementNotificationManager()),
    )

    private lateinit var underTest: PuzzleRushViewModel

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
        fun `GIVEN puzzles available WHEN viewModel initialized THEN uiState is Ready`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())

            buildVm()

            val state = underTest.uiState.value
            assertTrue(state is PuzzleRushUiState.Ready,
                "Expected Ready but got ${state::class.simpleName}")
            val ready = state as PuzzleRushUiState.Ready
            assertEquals(DEFAULT_RATING, ready.data.rating)
            assertEquals(Side.BLACK, ready.data.player)
            assertEquals("03:00", ready.time.value)
            assertFalse(ready.time.danger)
        }

        @Test
        fun `GIVEN no puzzles WHEN viewModel initialized THEN session ends gracefully`() = runTest(testDispatcher) {
            buildVm()

            assertTrue(underTest.uiState.value is PuzzleRushUiState.Finished)
        }

        @Test
        fun `GIVEN high score is 5 WHEN initialized THEN high score is loaded`() = runTest(testDispatcher) {
            userRepository.local.saveUser(User(highScores = User.HighScores(puzzleRush = 5)))
            puzzleSeries.enqueue(buildPuzzle())

            buildVm()

            assertTrue(underTest.uiState.value is PuzzleRushUiState.Ready)
        }
    }

    @Nested
    internal inner class Playing {
        @Test
        fun `GIVEN ready state WHEN first move made THEN timer starts`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle(moves = FOUR_MOVE_PUZZLE))
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e5)

            assertEquals(1, countdownTimer.startCount)
            assertEquals(RUSH_DURATION_MS, countdownTimer.lastDurationMs)
        }

        @Test
        fun `GIVEN playing WHEN correct move made THEN puzzle solved and next loaded`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            puzzleSeries.enqueue(buildPuzzle(rating = 1300))
            buildVm()

            makeMove(Locus.e7, Locus.e5)

            val state = underTest.uiState.value as PuzzleRushUiState.WithBoard
            assertEquals(1, state.results.size)
            assertTrue(state.results.first().success)
            assertEquals(1300, state.data.rating)
        }

        @Test
        fun `GIVEN playing WHEN wrong move made THEN rush ends with failure`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e6)

            val state = underTest.uiState.value as PuzzleRushUiState.Finished
            assertEquals(1, state.results.size)
            assertFalse(state.results.first().success)
            assertTrue(state.showSummaryDialog)
        }
    }

    @Nested
    internal inner class Timer {
        @Test
        fun `GIVEN playing WHEN timer ticks THEN remaining time updates`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle(moves = FOUR_MOVE_PUZZLE))
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e5)

            countdownTimer.emit(CountdownTimer.Remainder(120, 0))
            advanceUntilIdle()

            val state = underTest.uiState.value as PuzzleRushUiState.Playing
            assertEquals("02:00", state.time.value)
            assertFalse(state.time.danger)
        }

        @Test
        fun `GIVEN playing WHEN timer below danger threshold THEN danger flag set`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle(moves = FOUR_MOVE_PUZZLE))
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e5)

            countdownTimer.emit(CountdownTimer.Remainder(15, 500))
            advanceUntilIdle()

            val state = underTest.uiState.value as PuzzleRushUiState.Playing
            assertTrue(state.time.danger)
            assertEquals("15.5", state.time.value)
        }

        @Test
        fun `GIVEN playing WHEN timer expires THEN rush finishes`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle(moves = FOUR_MOVE_PUZZLE))
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e5)

            countdownTimer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertTrue(underTest.uiState.value is PuzzleRushUiState.Finished)
        }
    }

    @Nested
    internal inner class AbandonFlow {
        @Test
        fun `GIVEN playing WHEN onNavigateBackPressed THEN abandon dialog shown`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()

            val handled = underTest.onNavigateBackPressed()

            assertTrue(handled)
            advanceUntilIdle()
            val state = underTest.uiState.value as PuzzleRushUiState.Playing
            assertTrue(state.showAbandonDialog)
        }

        @Test
        fun `GIVEN abandon dialog shown WHEN dismissed THEN dialog hidden`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()
            underTest.onNavigateBackPressed()
            advanceUntilIdle()

            underTest.onAbandonDismissed()
            advanceUntilIdle()

            val state = underTest.uiState.value as PuzzleRushUiState.Playing
            assertFalse(state.showAbandonDialog)
        }

        @Test
        fun `GIVEN abandon dialog shown WHEN confirmed THEN rush ends`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()
            underTest.onNavigateBackPressed()
            advanceUntilIdle()

            underTest.onAbandonConfirmed()
            advanceUntilIdle()

            assertTrue(underTest.uiState.value is PuzzleRushUiState.Finished)
        }

        @Test
        fun `GIVEN not playing WHEN onNavigateBackPressed THEN returns false`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            val handled = underTest.onNavigateBackPressed()

            assertFalse(handled)
        }
    }

    @Nested
    internal inner class Completion {
        @Test
        fun `WHEN rush ended via wrong move THEN user stats updated`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e6)

            val user = userRepository.get()
            assertEquals(1, user.statistics.puzzleRushSessions)
            assertEquals(0, user.statistics.rushPuzzlesSolved)
            assertEquals(1, user.statistics.puzzlesPlayed)
            assertEquals(0, user.statistics.puzzlesSolved)
        }

        @Test
        fun `WHEN 1 puzzle solved with failure THEN user stats reflect both`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            puzzleSeries.enqueue(buildPuzzle(rating = 1300))
            buildVm()

            makeMove(Locus.e7, Locus.e5)
            makeMove(Locus.e7, Locus.e6)

            val user = userRepository.get()
            assertEquals(1, user.statistics.puzzleRushSessions)
            assertEquals(1, user.statistics.rushPuzzlesSolved)
            assertEquals(2, user.statistics.puzzlesPlayed)
            assertEquals(1, user.statistics.puzzlesSolved)
        }

        @Test
        fun `WHEN rush ended THEN high score updated if better`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            puzzleSeries.enqueue(buildPuzzle(rating = 1300))
            buildVm()

            makeMove(Locus.e7, Locus.e5)
            makeMove(Locus.e7, Locus.e6)

            val user = userRepository.get()
            assertEquals(1, user.highScores.puzzleRush)
        }

        @Test
        fun `GIVEN existing high score 5 WHEN rush ends with 1 solved THEN high score unchanged`() = runTest(testDispatcher) {
            userRepository.local.saveUser(User(highScores = User.HighScores(puzzleRush = 5)))
            puzzleSeries.enqueue(buildPuzzle())
            puzzleSeries.enqueue(buildPuzzle(rating = 1300))
            buildVm()

            makeMove(Locus.e7, Locus.e5)
            makeMove(Locus.e7, Locus.e6)

            val user = userRepository.get()
            assertEquals(5, user.highScores.puzzleRush)
        }

        @Test
        fun `WHEN rush ended via wrong move THEN failed puzzle IDs stored`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e6)

            val user = userRepository.get()
            assertTrue(user.failedPuzzles.isNotEmpty())
        }

        @Test
        fun `GIVEN rush ended WHEN onDismissSummary THEN dialog hidden`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e6)

            assertTrue((underTest.uiState.value as PuzzleRushUiState.Finished).showSummaryDialog)

            underTest.onDismissSummary()
            advanceUntilIdle()

            assertFalse((underTest.uiState.value as PuzzleRushUiState.Finished).showSummaryDialog)
        }

        @Test
        fun `GIVEN high score 0 and 1 solved WHEN rush ends THEN isNewHighScore true`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            puzzleSeries.enqueue(buildPuzzle(rating = 1300))
            buildVm()

            makeMove(Locus.e7, Locus.e5)
            makeMove(Locus.e7, Locus.e6)

            val state = underTest.uiState.value as PuzzleRushUiState.Finished
            assertTrue(state.isNewHighScore)
        }

        @Test
        fun `GIVEN high score 5 WHEN rush ends with 0 solved THEN isNewHighScore false`() = runTest(testDispatcher) {
            userRepository.local.saveUser(User(highScores = User.HighScores(puzzleRush = 5)))
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e6)

            val state = underTest.uiState.value as PuzzleRushUiState.Finished
            assertFalse(state.isNewHighScore)
        }
    }

    @Nested
    internal inner class PlayAgain {
        @Test
        fun `GIVEN finished WHEN onPlayAgain THEN new rush starts`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            makeMove(Locus.e7, Locus.e6)

            assertTrue(underTest.uiState.value is PuzzleRushUiState.Finished)

            puzzleSeries.enqueue(buildPuzzle(rating = 1400))

            underTest.onPlayAgain()
            advanceUntilIdle()

            assertTrue(underTest.uiState.value is PuzzleRushUiState.Ready)
        }
    }

    @Nested
    internal inner class Navigation {
        @Test
        fun `GIVEN finished WHEN onAnalyzeFailedPuzzle THEN navigation event dispatched`() = runTest(testDispatcher) {
            puzzleSeries.enqueue(buildPuzzle())
            buildVm()

            var destination: NavigationDispatcher.Destination? = null
            val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                navDispatcher.navigationEvents.collect { destination = it }
            }

            underTest.onAnalyzeFailedPuzzle(42)

            assertNotNull(destination)
            assertEquals(42, (destination as NavigationDispatcher.Destination.Analysis).puzzleId)
            collectJob.cancel()
        }
    }

    private fun TestScope.buildVm(series: GetBufferedPuzzleSeries = puzzleSeries) {
        underTest = PuzzleRushViewModel(
            defaultDispatcher = testDispatcher,
            adapter = PuzzleRushUiStateAdapter(),
            userRepository = userRepository,
            navDispatcher = navDispatcher,
            sounds = SoundCoordinator(),
            timer = countdownTimer,
            getBufferedPuzzleSeries = series,
            onPuzzleRushComplete = onPuzzleRushComplete,
            appSettingsRepository = appSettingsRepository,
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

    private var nextId = 1
    private fun buildPuzzle(
        rating: Int = DEFAULT_RATING,
        moves: List<String> = listOf("e2e4", "e7e5"),
    ): Puzzle = RealGameFactory().builder()
        .withDefaultBoard()
        .withRating(rating)
        .withId(nextId++)
        .withMoves(moves)
        .buildPuzzle()

    private companion object {
        const val DEFAULT_RATING = 1200
        val FOUR_MOVE_PUZZLE = listOf("e2e4", "e7e5", "g1f3", "b8c6")
    }
}

private class QueuedPuzzleSeries : GetBufferedPuzzleSeries {
    private val puzzles = ArrayDeque<Puzzle>()

    fun enqueue(puzzle: Puzzle) {
        puzzles.addLast(puzzle)
    }

    override fun execute(bufferSize: Int, ratingStart: Int, increment: Int): Flow<Puzzle> = flow {
        while (puzzles.isNotEmpty()) {
            emit(puzzles.removeFirst())
        }
    }
}
