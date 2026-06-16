package com.paulcraciunas.screens.boardvis.pieces.vm

import com.paulcraciunas.domain.api.boardvis.GetKnightPathBufferedSeries
import com.paulcraciunas.domain.api.boardvis.KnightPathExercise
import com.paulcraciunas.domain.api.boardvis.KnightPathResult
import com.paulcraciunas.domain.api.boardvis.OnKnightPathComplete
import com.paulcraciunas.domain.api.general.SchedulerBackedTestClock
import com.paulcraciunas.domain.impl.general.RealCountdownTimer
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.global.sounds.SoundCoordinator
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
import kotlinx.coroutines.test.advanceTimeBy
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
internal class KnightPathViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val userRepository = FakeUserRepository()
    private val fakeExerciseSeries = FakeGetKnightPathBufferedSeries()
    private val fakeOnComplete = FakeOnKnightPathComplete()

    private lateinit var underTest: KnightPathViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = KnightPathViewModel(
            dispatcher = testDispatcher,
            userRepository = userRepository,
            sounds = SoundCoordinator(),
            appSettingsRepository = FakeAppSettingsRepository(),
            exercises = fakeExerciseSeries,
            onKnightPathComplete = fakeOnComplete,
            countdownTimer = RealCountdownTimer(SchedulerBackedTestClock(testDispatcher.scheduler)),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN initial state WHEN viewModel created THEN uiState is Setup`() = knightPathTest {
        assertTrue(underTest.uiState.value is KnightPathUiState.Setup)
    }

    @Test
    fun `GIVEN setup state WHEN onPlayClicked THEN game starts`() = knightPathTest {
        fakeExerciseSeries.with(twoMoveExercise())

        underTest.onPlayClicked()
        advanceTimeBy(1001L)

        val uiState = underTest.uiState.value
        assertTrue(uiState is KnightPathUiState.Playing)
        val playing = uiState as KnightPathUiState.Playing
        assertEquals(0, playing.score)
        assertEquals("29.0", playing.timeRemaining.value)
    }

    @Test
    fun `GIVEN playing state WHEN correct intermediate move made THEN position advances`() = knightPathTest {
        val exercise = twoMoveExercise()
        fakeExerciseSeries.with(exercise)

        underTest.onPlayClicked()
        advanceTimeBy(1000L)

        underTest.onSquareClicked(exercise.path[1])
        advanceTimeBy(1000L)

        val uiState = underTest.uiState.value
        assertTrue(uiState is KnightPathUiState.Playing)
        assertEquals(0, (uiState as KnightPathUiState.Playing).score)
    }

    @Test
    fun `GIVEN playing state WHEN exercise completed THEN score increments and new exercise loads`() = knightPathTest {
        val exercise = twoMoveExercise()
        fakeExerciseSeries.with(exercise, twoMoveExercise(
            start = Locus.a1,
            destination = Locus.c2,
            path = listOf(Locus.a1, Locus.b3, Locus.c2),
        ))

        underTest.onPlayClicked()
        advanceTimeBy(1000L)

        underTest.onSquareClicked(exercise.path[1])
        advanceTimeBy(1000L)
        underTest.onSquareClicked(exercise.path[2])
        advanceTimeBy(1000L)

        val uiState = underTest.uiState.value
        assertTrue(uiState is KnightPathUiState.Playing)
        val playing = uiState as KnightPathUiState.Playing
        assertEquals(1, playing.score)
    }

    @Test
    fun `GIVEN playing state WHEN wrong empty square clicked THEN game ends and timer stops`() = knightPathTest {
        fakeExerciseSeries.with(twoMoveExercise())

        underTest.onPlayClicked()
        advanceTimeBy(1000L)

        underTest.onSquareClicked(Locus.a1)
        advanceTimeBy(1000L)

        val uiState = underTest.uiState.value
        assertTrue(uiState is KnightPathUiState.GameOver)
        assertTrue((uiState as KnightPathUiState.GameOver).wasWrongMove)
        assertTrue(fakeOnComplete.invoked)
    }

    @Test
    fun `GIVEN playing state WHEN occupied square clicked THEN state unchanged`() = knightPathTest {
        fakeExerciseSeries.with(twoMoveExercise())

        underTest.onPlayClicked()
        advanceTimeBy(1000L)

        underTest.onSquareClicked(Locus.d2)
        advanceTimeBy(1000L)

        val uiState = underTest.uiState.value
        assertTrue(uiState is KnightPathUiState.Playing)
        assertEquals(0, (uiState as KnightPathUiState.Playing).score)
    }

    @Test
    fun `GIVEN playing state WHEN timer expires THEN game over without wrong move`() = knightPathTest {
        fakeExerciseSeries.with(twoMoveExercise())

        underTest.onPlayClicked()
        advanceToEnd()

        val uiState = underTest.uiState.value
        assertTrue(uiState is KnightPathUiState.GameOver)
        assertFalse((uiState as KnightPathUiState.GameOver).wasWrongMove)
        assertTrue(fakeOnComplete.invoked)
    }

    @Test
    fun `GIVEN score above high score WHEN game ends THEN isNewHighScore true`() = knightPathTest {
        saveUser(User(highScores = User.HighScores(knightPath = 0)))
        val exercise = twoMoveExercise()
        fakeExerciseSeries.with(exercise, twoMoveExercise(
            start = Locus.a1,
            destination = Locus.c2,
            path = listOf(Locus.a1, Locus.b3, Locus.c2),
        ))

        underTest.onPlayClicked()
        advanceTimeBy(1000L)

        underTest.onSquareClicked(exercise.path[1])
        advanceTimeBy(1000L)
        underTest.onSquareClicked(exercise.path[2])
        advanceToEnd()

        val uiState = underTest.uiState.value
        assertTrue(uiState is KnightPathUiState.GameOver)
        assertTrue((uiState as KnightPathUiState.GameOver).isNewHighScore)
    }

    @Test
    fun `GIVEN score below high score WHEN game ends THEN isNewHighScore false`() = knightPathTest {
        saveUser(User(highScores = User.HighScores(knightPath = 100)))
        fakeExerciseSeries.with(twoMoveExercise())

        underTest.onPlayClicked()
        advanceToEnd()

        val uiState = underTest.uiState.value
        assertTrue(uiState is KnightPathUiState.GameOver)
        assertFalse((uiState as KnightPathUiState.GameOver).isNewHighScore)
    }

    @Test
    fun `GIVEN game over WHEN onPlayAgain called THEN returns to setup state`() = knightPathTest {
        fakeExerciseSeries.with(twoMoveExercise())
        underTest.onPlayClicked()
        advanceToEnd()

        underTest.onPlayAgain()
        advanceTimeBy(1000L)

        assertTrue(underTest.uiState.value is KnightPathUiState.Setup)
    }

    @Test
    fun `GIVEN timer ends WHEN onComplete called THEN result has correct score`() = knightPathTest {
        val exercise = twoMoveExercise()
        fakeExerciseSeries.with(exercise, twoMoveExercise(
            start = Locus.a1,
            destination = Locus.c2,
            path = listOf(Locus.a1, Locus.b3, Locus.c2),
        ))

        underTest.onPlayClicked()
        advanceTimeBy(1000L)

        underTest.onSquareClicked(exercise.path[1])
        advanceTimeBy(1000L)
        underTest.onSquareClicked(exercise.path[2])
        advanceToEnd()

        assertTrue(fakeOnComplete.invoked)
        assertEquals(1, fakeOnComplete.lastResult?.score)
    }

    @Test
    fun `GIVEN playing WHEN onPlayAgain called mid-game THEN continue playing`() = knightPathTest {
        fakeExerciseSeries.with(twoMoveExercise())
        underTest.onPlayClicked()
        advanceTimeBy(1000L)

        underTest.onPlayAgain()
        advanceTimeBy(1000L)

        assertTrue(underTest.uiState.value is KnightPathUiState.Playing)
        assertFalse(fakeOnComplete.invoked)
    }

    private fun knightPathTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            underTest.uiState.collect {}
        }
        block()
    }

    private fun TestScope.saveUser(user: User) {
        launch { userRepository.local.saveUser(user) }
        testScheduler.runCurrent()
    }

    private fun TestScope.advanceToEnd() {
        advanceTimeBy((KnightPathUiState.DEFAULT_DURATION_SECONDS + 1) * 1000L)
    }

    private fun twoMoveExercise(
        start: Locus = Locus.d2,
        destination: Locus = Locus.d4,
        path: List<Locus> = listOf(Locus.d2, Locus.f3, Locus.d4),
    ): KnightPathExercise = KnightPathExercise(
        board = Board().apply {
            add(Piece.Knight, Side.WHITE, start)
            add(Piece.Pawn, Side.BLACK, destination)
        },
        from = start,
        path = path,
    )
}

private class FakeGetKnightPathBufferedSeries : GetKnightPathBufferedSeries {
    private val exercises = mutableListOf<KnightPathExercise>()

    fun with(vararg exercises: KnightPathExercise) {
        this.exercises.addAll(exercises)
    }

    override fun invoke(bufferSize: Int): Flow<KnightPathExercise> = flow {
        if (exercises.isEmpty()) throw IllegalStateException("No exercise configured")
        exercises.forEach {
            emit(it)
        }
    }
}

private class FakeOnKnightPathComplete : OnKnightPathComplete {
    var invoked = false
        private set
    var lastResult: KnightPathResult? = null
        private set

    override suspend fun invoke(result: KnightPathResult) {
        invoked = true
        lastResult = result
    }
}
