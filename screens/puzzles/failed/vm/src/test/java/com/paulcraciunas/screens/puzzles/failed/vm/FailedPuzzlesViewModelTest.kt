package com.paulcraciunas.screens.puzzles.failed.vm

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.general.FakeTimer
import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.domain.impl.puzzles.OnFailedPuzzleCompleteImpl
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.global.navigation.NavigationDispatcher
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class FailedPuzzlesViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val gameFactory = RealGameFactory()

    private val getFailedPuzzles = FakeGetFailedPuzzles()
    private val timer = FakeTimer()
    private val appSettingsRepository = FakeAppSettingsRepository.default()
    private val userRepository = FakeUserRepository()
    private val navDispatcher = NavigationDispatcher()
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
        fun `GIVEN puzzles available WHEN initialized THEN uiState is Playing`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
            assertEquals(DEFAULT_RATING, state.data.rating)
            assertEquals(Side.BLACK, state.data.player)
            assertEquals(0, state.progress.solved)
            assertEquals(0, state.progress.total)
        }

        @Test
        fun `GIVEN no puzzles WHEN initialized THEN uiState is Empty`() = runTest {
            val underTest = buildVm()

            assertEquals(FailedPuzzlesUiState.Empty, underTest.uiState.value)
        }

        @Test
        fun `GIVEN getFailedPuzzles throws WHEN initialized THEN uiState is Failed`() = runTest {
            getFailedPuzzles.withFailure(RuntimeException("boom"))
            val underTest = buildVm()

            assertEquals(FailedPuzzlesUiState.Failed, underTest.uiState.value)
        }

        @Test
        fun `GIVEN puzzles available WHEN initialized THEN timer is started`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            buildVm()

            assertTrue(timer.isRunning())
        }

        @Test
        fun `GIVEN puzzles available WHEN initialized THEN opponent first move is played`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
            assertTrue(state.data.boardData.at(Locus.e4).piece != null)
        }
    }

    @Nested
    internal inner class PlayerInteraction {
        @Test
        fun `GIVEN no selection WHEN onSquareClicked THEN piece is selected`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            underTest.onSquareClicked(Locus.e7)
            testDispatcher.scheduler.runCurrent()

            val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
            assertEquals(true, state.data.boardData.at(Locus.e7).piece?.isSelected)
        }

        @Test
        fun `GIVEN piece selected WHEN same square clicked THEN deselects`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            underTest.onSquareClicked(Locus.e7)
            testDispatcher.scheduler.runCurrent()
            underTest.onSquareClicked(Locus.e7)
            testDispatcher.scheduler.runCurrent()

            val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
            assertEquals(false, state.data.boardData.at(Locus.e7).piece?.isSelected)
        }

        @Test
        fun `GIVEN correct move WHEN played THEN opponent responds`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle(), buildStandardPuzzle(rating = 1300))
            val underTest = buildVm()

            makeMove(underTest, from = Locus.e7, to = Locus.e5)

            val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
            assertTrue(state.data.boardData.at(Locus.f3).piece != null)
        }

        @Test
        fun `GIVEN all correct moves WHEN puzzle solved THEN advances to next`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle(), buildStandardPuzzle(rating = 1300))
            val underTest = buildVm()

            makeMove(underTest, from = Locus.e7, to = Locus.e5)
            makeMove(underTest, from = Locus.b8, to = Locus.c6)

            val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
            assertEquals(1, state.results.size)
            assertTrue(state.results.first().success)
            assertEquals(1, state.progress.solved)
        }

        @Test
        fun `GIVEN wrong move WHEN played THEN advances to next with failure`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle(), buildStandardPuzzle(rating = 1300))
            val underTest = buildVm()

            makeMove(underTest, from = Locus.d7, to = Locus.d5)

            val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
            assertEquals(1, state.results.size)
            assertFalse(state.results.first().success)
            assertEquals(0, state.progress.solved)
        }
    }

    @Nested
    internal inner class Completion {
        @Test
        fun `GIVEN single puzzle WHEN solved THEN state is Finished with dialog`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            makeMove(underTest, from = Locus.e7, to = Locus.e5)
            makeMove(underTest, from = Locus.b8, to = Locus.c6)

            val state = underTest.uiState.value as FailedPuzzlesUiState.Finished
            assertTrue(state.showCompletionDialog)
            assertEquals(1, state.results.size)
            assertTrue(state.results.first().success)
            assertEquals(1, state.progress.solved)
            assertEquals(1, state.progress.total)
        }

        @Test
        fun `GIVEN single puzzle WHEN failed THEN state is Finished`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            makeMove(underTest, from = Locus.d7, to = Locus.d5)

            val state = underTest.uiState.value as FailedPuzzlesUiState.Finished
            assertEquals(1, state.results.size)
            assertFalse(state.results.first().success)
            assertEquals(0, state.progress.solved)
        }

        @Test
        fun `GIVEN finished state WHEN onDismissCompletion THEN dialog is hidden`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            makeMove(underTest, from = Locus.e7, to = Locus.e5)
            makeMove(underTest, from = Locus.b8, to = Locus.c6)

            assertTrue((underTest.uiState.value as FailedPuzzlesUiState.Finished).showCompletionDialog)

            underTest.onDismissCompletion()
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse((underTest.uiState.value as FailedPuzzlesUiState.Finished).showCompletionDialog)
        }

        @Test
        fun `GIVEN multiple puzzles WHEN all solved THEN progress shows total`() = runTest {
            getFailedPuzzles.withPuzzles(
                buildStandardPuzzle(),
                buildStandardPuzzle(rating = 1300),
            )
            val underTest = buildVm()

            makeMove(underTest, from = Locus.e7, to = Locus.e5)
            makeMove(underTest, from = Locus.b8, to = Locus.c6)

            makeMove(underTest, from = Locus.e7, to = Locus.e5)
            makeMove(underTest, from = Locus.b8, to = Locus.c6)

            val state = underTest.uiState.value as FailedPuzzlesUiState.Finished
            assertEquals(2, state.progress.solved)
            assertEquals(2, state.progress.total)
        }
    }

    @Nested
    internal inner class CompletionTracking {
        @Test
        fun `GIVEN puzzle solved WHEN completed THEN puzzle removed from failed list`() = runTest {
            val puzzleId = 42
            userRepository.local.saveUser(
                userRepository.get().copy(failedPuzzles = listOf(puzzleId, 99))
            )
            getFailedPuzzles.withPuzzles(buildStandardPuzzle(id = puzzleId))
            val underTest = buildVm()

            makeMove(underTest, from = Locus.e7, to = Locus.e5)
            makeMove(underTest, from = Locus.b8, to = Locus.c6)

            val updatedUser = userRepository.get()
            assertFalse(updatedUser.failedPuzzles.contains(puzzleId))
            assertTrue(updatedUser.failedPuzzles.contains(99))
            assertEquals(1, updatedUser.statistics.failedPuzzlesRedeemed)
        }

        @Test
        fun `GIVEN puzzle failed WHEN completed THEN puzzle stays in failed list`() = runTest {
            val puzzleId = 42
            userRepository.local.saveUser(
                userRepository.get().copy(failedPuzzles = listOf(puzzleId))
            )
            getFailedPuzzles.withPuzzles(buildStandardPuzzle(id = puzzleId))
            val underTest = buildVm()

            makeMove(underTest, from = Locus.d7, to = Locus.d5)

            val updatedUser = userRepository.get()
            assertTrue(updatedUser.failedPuzzles.contains(puzzleId))
            assertEquals(0, updatedUser.statistics.failedPuzzlesRedeemed)
        }
    }

    @Nested
    internal inner class TimerLifecycle {
        @Test
        fun `GIVEN playing WHEN onStop THEN timer pauses`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            underTest.onStop()

            assertFalse(timer.isRunning())
        }

        @Test
        fun `GIVEN timer paused WHEN onStart THEN timer resumes`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            underTest.onStop()
            underTest.onStart()

            assertTrue(timer.isRunning())
        }
    }

    @Nested
    internal inner class Navigation {
        @Test
        fun `GIVEN finished WHEN onAnalyzeFailedPuzzle THEN navigates to analysis`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle(id = 7))
            val underTest = buildVm()

            makeMove(underTest, from = Locus.d7, to = Locus.d5)

            var destination: NavigationDispatcher.Destination? = null
            backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
                navDispatcher.navigationEvents.collect { destination = it }
            }

            underTest.onAnalyzeFailedPuzzle(7)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(NavigationDispatcher.Destination.Analysis(7), destination)
        }
    }

    @Nested
    internal inner class Hints {
        @Test
        fun `GIVEN puzzle loaded WHEN onHintRequested THEN correct piece highlighted`() = runTest {
            getFailedPuzzles.withPuzzles(buildStandardPuzzle())
            val underTest = buildVm()

            underTest.onHintRequested()
            testDispatcher.scheduler.runCurrent()

            val state = underTest.uiState.value as FailedPuzzlesUiState.Playing
            assertEquals(true, state.data.boardData.at(Locus.e7).piece?.isSelected)
        }
    }

    private fun TestScope.buildVm(): FailedPuzzlesViewModel {
        val vm = FailedPuzzlesViewModel(
            timer = timer,
            navDispatcher = navDispatcher,
            dispatcher = testDispatcher,
            getFailedPuzzles = getFailedPuzzles,
            onFailedPuzzleComplete = OnFailedPuzzleCompleteImpl(userRepository, noOpAchievements),
            appSettingsRepository = appSettingsRepository,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            vm.uiState.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    private fun makeMove(vm: FailedPuzzlesViewModel, from: Locus, to: Locus) {
        vm.onSquareClicked(from)
        testDispatcher.scheduler.runCurrent()
        vm.onSquareClicked(to)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun buildStandardPuzzle(
        id: Int = DEFAULT_ID,
        rating: Int = DEFAULT_RATING,
        moves: List<String> = listOf("e2e4", "e7e5", "g1f3", "b8c6"),
    ): Puzzle = gameFactory.builder()
        .withId(id)
        .withDefaultBoard()
        .withRating(rating)
        .withMoves(moves)
        .buildPuzzle()

    private companion object {
        private const val DEFAULT_ID = 42
        private const val DEFAULT_RATING = 1200
    }
}

private class FakeGetFailedPuzzles : GetFailedPuzzles {
    private var puzzles: List<Puzzle> = emptyList()
    private var exception: Exception? = null

    fun withPuzzles(vararg puzzles: Puzzle) {
        this.puzzles = puzzles.toList()
    }

    fun withFailure(exception: Exception) {
        this.exception = exception
    }

    override fun execute(bufferSize: Int): Flow<Puzzle> = flow {
        exception?.let { throw it }
        puzzles.forEach { emit(it) }
    }
}
