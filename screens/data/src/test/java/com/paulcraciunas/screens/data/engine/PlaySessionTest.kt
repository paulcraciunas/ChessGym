package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.FakeCountdownTimer
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.GameNavigation
import com.paulcraciunas.screens.data.GamePlayableBoard
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.NoOpOpponent
import com.paulcraciunas.screens.data.NoOpSolution
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.screens.data.PuzzleSolution
import com.paulcraciunas.screens.data.ScriptedOpponent
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlaySessionTest {
    private val gameFactory = RealGameFactory()
    private val timer = FakeCountdownTimer()
    private val settingsRepository = FakeAppSettingsRepository.default()

    @Nested
    internal inner class Initialization {
        @Test
        fun `GIVEN play session WHEN created THEN state is Loading`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            assertEquals(PlaySessionState.Status.Loading, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN play session WHEN run called THEN state transitions to Ready`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ready, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN play session WHEN run THEN board state is populated`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertNotNull(playSession.stateValue.boardState)
            assertEquals(Side.BLACK, playSession.stateValue.boardState.player)
        }

        @Test
        fun `GIVEN play session WHEN run THEN opponent first move is played`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            val board = playSession.stateValue.boardState
            assertNotNull(board.boardData.at(Locus.e4))
        }

        @Test
        fun `GIVEN play session WHEN run with no animations THEN isAnimating is false`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertFalse(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN session source throws WHEN run THEN state is Failed`() = runTest {
            val failingSource = PlaySession.SessionsSource {
                flow { throw RuntimeException("DB failure") }
            }
            val playSession = buildPlaySession(sessions = failingSource)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Failed, playSession.stateValue.status)
            assertEquals("DB failure", playSession.stateValue.errorMessage)
        }

        @Test
        fun `GIVEN play session WHEN run THEN loads settings`() = runTest {
            val session = buildSession(buildPuzzle())
            val playSession = buildPlaySession(sessions = singleSessionSource(session), autoPromote = false)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertFalse(session.autoPromote)
        }

        @Test
        fun `GIVEN play session with hints configured WHEN run THEN hintAvailable is true`() = runTest {
            val config = noAnimationsConfig().copy(hints = PlaySessionConfiguration.HintMode.Single)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertTrue(playSession.stateValue.hintAvailable)
        }

        @Test
        fun `GIVEN play session without hints WHEN run THEN hintAvailable is false`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertFalse(playSession.stateValue.hintAvailable)
        }
    }

    @Nested
    internal inner class PlayerMoves {
        @Test
        fun `GIVEN Ready state WHEN first move accepted THEN status transitions to Playing`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN playing WHEN selecting own piece THEN board shows selection`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()

            val board = playSession.stateValue.boardState
            assertEquals(Locus.e7, board.boardData.selection)
        }

        @Test
        fun `GIVEN piece selected WHEN valid move THEN move is played`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)

            assertTrue(playSession.stateValue.boardState.movePlayed)
        }

        @Test
        fun `GIVEN correct move played WHEN no animations THEN opponent responds immediately`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)

            assertNotNull(playSession.stateValue.boardState.boardData.at(Locus.f3))
        }

        @Test
        fun `GIVEN wrong move played WHEN OnFirstFailure mode THEN session ends`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN wrong move WHEN OnFirstFailure THEN results contain failure`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            val results = playSession.stateValue.results
            assertEquals(1, results.size)
            assertFalse(results.first().success)
        }

        @Test
        fun `GIVEN all correct moves WHEN puzzle solved THEN session succeeds`() = runTest {
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            val results = playSession.stateValue.results
            assertEquals(1, results.size)
            assertTrue(results.first().success)
        }

        @Test
        fun `GIVEN clicking empty square WHEN no selection THEN nothing happens`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceUntilIdle()

            assertNull(playSession.stateValue.boardState.boardData.selection)
        }

        @Test
        fun `GIVEN clicking opponent piece WHEN no selection THEN nothing happens`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e2))
            advanceUntilIdle()

            assertNull(playSession.stateValue.boardState.boardData.selection)
        }
    }

    @Nested
    internal inner class Animations {
        @Test
        fun `GIVEN animations enabled WHEN move played THEN isAnimating becomes true`() = runTest {
            val playSession = buildPlaySession(config = animationsConfig(), sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)

            assertTrue(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN animating WHEN animation completes THEN isAnimating becomes false`() = runTest {
            val playSession = buildPlaySession(config = animationsConfig(moveMs = 50), sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceTimeBy(200)

            assertFalse(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN animating WHEN user clicks THEN click is ignored`() = runTest {
            val playSession = buildPlaySession(config = animationsConfig(moveMs = 100), sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceTimeBy(10)

            assertTrue(playSession.stateValue.isAnimating)
            playSession.accept(PlayIntent.SelectSquare(Locus.b8))
            advanceTimeBy(10)

            val board = playSession.stateValue.boardState
            assertNull(board.boardData.selection)
        }

        @Test
        fun `GIVEN second session WHEN animations on THEN board swap animation plays`() = runTest {
            val config = animationsConfig(moveMs = 50, boardSwapMs = 50).copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            playSession.accept(PlayIntent.SelectSquare(Locus.b8))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.c6))
            advanceTimeBy(10)

            assertTrue(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN animations enabled WHEN opponent move THEN animation delay is respected`() = runTest {
            val playSession = buildPlaySession(config = animationsConfig(moveMs = 100), sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceTimeBy(50)
            assertTrue(playSession.stateValue.isAnimating)

            advanceTimeBy(60)
            assertTrue(playSession.stateValue.isAnimating)

            advanceTimeBy(100)
            assertFalse(playSession.stateValue.isAnimating)
        }
    }

    @Nested
    internal inner class Timer {
        @Test
        fun `GIVEN timed config WHEN run THEN remaining time is set`() = runTest {
            val playSession = buildPlaySession(config = timedConfig(durationMs = 5000L), sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertEquals(5000L, playSession.stateValue.remainingTimeMs)
        }

        @Test
        fun `GIVEN StartOnClick timer WHEN first move THEN timer starts`() = runTest {
            val config = timedConfig(durationMs = 3000L, mode = PlaySessionConfiguration.TimedMode.StartOnClick)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertEquals(3000L, playSession.stateValue.remainingTimeMs)

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            timer.emit(CountdownTimer.Remainder(2, 800))
            advanceUntilIdle()

            assertTrue(playSession.stateValue.remainingTimeMs!! < 3000L)
        }

        @Test
        fun `GIVEN StartImmediately timer WHEN run THEN timer starts without click`() = runTest {
            val config = timedConfig(durationMs = 3000L, mode = PlaySessionConfiguration.TimedMode.StartImmediately)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            timer.emit(CountdownTimer.Remainder(2, 500))
            advanceUntilIdle()

            assertTrue(playSession.stateValue.remainingTimeMs!! < 3000L)
        }

        @Test
        fun `GIVEN timer running WHEN time expires THEN game ends`() = runTest {
            val config = timedConfig(durationMs = 500L, mode = PlaySessionConfiguration.TimedMode.StartImmediately)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN timer expires WHEN mid puzzle THEN incomplete puzzle not counted in results`() = runTest {
            val config = timedConfig(durationMs = 500L, mode = PlaySessionConfiguration.TimedMode.StartImmediately)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
            assertTrue(playSession.stateValue.results.isEmpty())
        }

        @Test
        fun `GIVEN timer expires WHEN during animation THEN game still ends`() = runTest {
            val config = animationsConfig(moveMs = 200).copy(
                timed = PlaySessionConfiguration.Timed(durationInMs = 300, mode = PlaySessionConfiguration.TimedMode.StartImmediately),
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceTimeBy(10)

            assertTrue(playSession.stateValue.isAnimating)
            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
            assertFalse(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN timer expires WHEN game ends THEN showSummary is true`() = runTest {
            val config = timedConfig(durationMs = 300L, mode = PlaySessionConfiguration.TimedMode.StartImmediately)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertTrue(playSession.stateValue.showSummary)
        }

        @Test
        fun `GIVEN timer expires WHEN remainingTimeMs checked THEN it is zero`() = runTest {
            val config = timedConfig(durationMs = 300L, mode = PlaySessionConfiguration.TimedMode.StartImmediately)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertEquals(0L, playSession.stateValue.remainingTimeMs)
        }

        @Test
        fun `GIVEN timer expires WHEN waiting for next session THEN game ends without waiting`() = runTest {
            val sessionFlow = MutableSharedFlow<BoardSession>()
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
                timed = PlaySessionConfiguration.Timed(durationInMs = 1000, mode = PlaySessionConfiguration.TimedMode.StartOnClick),
            )
            val playSession = buildPlaySession(config = config, sessions = controllableSessionSource(sessionFlow), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            sessionFlow.emit(buildSession(buildPuzzle()))
            advanceUntilIdle()
            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)

            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
            assertTrue(playSession.stateValue.showSummary)
        }

        @Test
        fun `GIVEN timer expires WHEN game ends THEN onPlayComplete callback fires`() = runTest {
            var callbackState: PlaySessionState? = null
            val config = timedConfig(durationMs = 300, mode = PlaySessionConfiguration.TimedMode.StartImmediately)
            val playSession = buildPlaySession(
                config = config,
                sessions = singleSessionSource(),
                timer = timer,
                onPlayComplete = { callbackState = it },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertNotNull(callbackState)
            assertEquals(PlaySessionState.Status.Ended, callbackState!!.status)
        }

        @Test
        fun `GIVEN puzzles solved WHEN timer expires on next puzzle THEN previous results preserved`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
                timed = PlaySessionConfiguration.Timed(durationInMs = 5000, mode = PlaySessionConfiguration.TimedMode.StartOnClick),
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(3), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertEquals(1, playSession.stateValue.results.size)
            assertTrue(playSession.stateValue.results.first().success)
        }
    }

    @Nested
    internal inner class MultipleSessions {
        @Test
        fun `GIVEN multiple puzzles WHEN first solved THEN advances to next`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
            assertEquals(1, playSession.stateValue.results.size)
        }

        @Test
        fun `GIVEN autoNext false WHEN session completes THEN status becomes Paused`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = false,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(PlaySessionState.Status.Paused, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN paused WHEN Resume sent THEN continues to next session`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = false,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(PlaySessionState.Status.Paused, playSession.stateValue.status)

            playSession.accept(PlayIntent.Resume)
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN paused WHEN non-resumable intent sent THEN it is ignored`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = false,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Paused, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN OnSourceExhausted WHEN puzzle failed THEN continues to next`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
            assertEquals(1, playSession.stateValue.results.size)
            assertFalse(playSession.stateValue.results.first().success)
        }

        @Test
        fun `GIVEN source exhausted WHEN flow completes THEN onPlayComplete called`() = runTest {
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            var completeCalled = false
            val playSession = buildPlaySession(
                config = config,
                sessions = singleSessionSource(),
                onPlayComplete = PlaySession.OnComplete { completeCalled = true },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertTrue(completeCalled)
        }

        @Test
        fun `GIVEN multiple sessions WHEN onSessionComplete configured THEN called after each`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
            )
            var sessionCompleteCount = 0
            val playSession = buildPlaySession(
                config = config,
                sessions = multiSessionSource(2),
                onSessionComplete = PlaySession.OnComplete { sessionCompleteCount++ },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(1, sessionCompleteCount)
        }

        @Test
        fun `GIVEN multiple puzzles solved WHEN all done THEN results accumulated`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            // Solve puzzle 1
            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            // Solve puzzle 2
            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(2, playSession.stateValue.results.size)
            assertTrue(playSession.stateValue.results.all { it.success })
        }
    }

    @Nested
    internal inner class Abandon {
        @Test
        fun `GIVEN playing WHEN RequestAbandon THEN abandonRequested is true`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.RequestAbandon)
            advanceUntilIdle()

            assertTrue(playSession.stateValue.abandonRequested)
        }

        @Test
        fun `GIVEN abandon requested WHEN DismissAbandon THEN abandonRequested is false`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.RequestAbandon)
            advanceUntilIdle()
            playSession.accept(PlayIntent.DismissAbandon)
            advanceUntilIdle()

            assertFalse(playSession.stateValue.abandonRequested)
        }

        @Test
        fun `GIVEN abandon confirmed WHEN no animations THEN session fails`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.ConfirmAbandon)
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN abandon confirmed WHEN animations and solution THEN solution plays out`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val playSession = buildPlaySession(config = animationsConfig(solutionMs = 50), sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.ConfirmAbandon)
            advanceTimeBy(10)

            assertTrue(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN solution playing WHEN fully animated THEN session ends as defeat`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val playSession = buildPlaySession(config = animationsConfig(solutionMs = 50), sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.ConfirmAbandon)
            advanceTimeBy(500)

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN abandon confirmed WHEN solution fully animated THEN result outcome is Lost`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle(id = 99, rating = 1600))
            val config = animationsConfig(solutionMs = 50).copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.ConfirmAbandon)
            advanceTimeBy(500)

            val result = playSession.stateValue.results.first()
            assertEquals(Outcome.Lost, result.outcome)
            assertFalse(result.success)
        }

        @Test
        fun `GIVEN abandon confirmed WHEN no animations THEN result outcome is Lost`() = runTest {
            val puzzle = buildPuzzle(id = 42, rating = 1200)
            val session = buildSession(puzzle)
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.ConfirmAbandon)
            advanceUntilIdle()

            val result = playSession.stateValue.results.first()
            assertEquals(Outcome.Lost, result.outcome)
        }

        @Test
        fun `GIVEN abandon confirmed WHEN solution animating THEN boardState outcome is Lost`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val config = animationsConfig(solutionMs = 100)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.ConfirmAbandon)
            advanceTimeBy(10)

            assertEquals(Outcome.Lost, playSession.stateValue.boardState.outcome)
        }

        @Test
        fun `GIVEN OnFirstFailure WHEN puzzle abandoned THEN game ends`() = runTest {
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnFirstFailure)
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.ConfirmAbandon)
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }
    }

    @Nested
    internal inner class Hints {
        @Test
        fun `GIVEN hints unlimited WHEN hint used THEN hintAvailable stays true`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val config = noAnimationsConfig().copy(hints = PlaySessionConfiguration.HintMode.Unlimited)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.Hint)
            advanceUntilIdle()

            assertTrue(playSession.stateValue.hintAvailable)
        }

        @Test
        fun `GIVEN hints single WHEN hint used THEN hintAvailable becomes false`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val config = noAnimationsConfig().copy(hints = PlaySessionConfiguration.HintMode.Single)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.Hint)
            advanceUntilIdle()

            assertFalse(playSession.stateValue.hintAvailable)
        }

        @Test
        fun `GIVEN hint requested WHEN solution available THEN correct square highlighted`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val config = noAnimationsConfig().copy(hints = PlaySessionConfiguration.HintMode.Single)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.Hint)
            advanceUntilIdle()

            val board = playSession.stateValue.boardState
            assertEquals(Locus.e7, board.boardData.selection)
        }
    }

    @Nested
    internal inner class ReEntrySafety {
        @Test
        fun `GIVEN session running WHEN run cancelled and restarted THEN new run processes sessions`() = runTest {
            val sessionFlow = MutableSharedFlow<BoardSession>()
            val playSession = buildPlaySession(sessions = controllableSessionSource(sessionFlow))

            val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            val puzzle = buildPuzzle()
            val session = buildSession(puzzle)
            job.cancel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            sessionFlow.emit(session)
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ready, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN session ended WHEN run called again THEN state resets to Loading`() = runTest {
            val puzzle = buildPuzzle()
            val session = buildSession(puzzle)
            val playSession = buildPlaySession(sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)

            val puzzle2 = buildPuzzle(id = 2)
            val session2 = buildSession(puzzle2)
            val newSource = singleSessionSource(session2)
            val playSession2 = buildPlaySession(sessions = newSource)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession2.run() }
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ready, playSession2.stateValue.status)
        }
    }

    @Nested
    internal inner class Callbacks {
        @Test
        fun `GIVEN onPlayComplete WHEN game ends THEN callback receives final state`() = runTest {
            var callbackState: PlaySessionState? = null
            val playSession = buildPlaySession(
                sessions = singleSessionSource(),
                onPlayComplete = PlaySession.OnComplete { callbackState = it },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertNotNull(callbackState)
            assertEquals(PlaySessionState.Status.Ended, callbackState!!.status)
        }

        @Test
        fun `GIVEN onPlayComplete WHEN game fails with exception THEN callback not called`() = runTest {
            val failingSource = PlaySession.SessionsSource {
                flow { throw RuntimeException("oops") }
            }
            var callbackCalled = false
            val playSession = buildPlaySession(
                sessions = failingSource,
                onPlayComplete = PlaySession.OnComplete { callbackCalled = true },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertFalse(callbackCalled)
        }

        @Test
        fun `GIVEN onSessionComplete WHEN session finishes THEN callback called with results`() = runTest {
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            var sessionState: PlaySessionState? = null
            val playSession = buildPlaySession(
                config = config,
                sessions = singleSessionSource(),
                onSessionComplete = PlaySession.OnComplete { sessionState = it },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertNotNull(sessionState)
            assertEquals(1, sessionState!!.results.size)
            assertTrue(sessionState.results.first().success)
        }
    }

    @Nested
    internal inner class Configuration {
        @Test
        fun `GIVEN play session with unlimited hints WHEN run THEN hints available`() = runTest {
            val puzzle = buildPuzzle()
            val session = buildSession(puzzle)
            val playSession = buildPlaySession(
                config = noAnimationsConfig().copy(hints = PlaySessionConfiguration.HintMode.Unlimited),
                sessions = singleSessionSource(session),
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertTrue(playSession.stateValue.hintAvailable)
        }

        @Test
        fun `GIVEN autoPromote in settings WHEN run THEN session uses setting`() = runTest {
            settingsRepository.updateAutoPromote(true)
            val session = buildSession(buildPuzzle())
            val playSession = buildPlaySession(sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertTrue(session.autoPromote)
        }

        @Test
        fun `GIVEN animations disabled in settings WHEN run THEN waitForAnimations is false`() = runTest {
            settingsRepository.updateEnableAnimations(false)
            val playSession = buildPlaySession(config = animationsConfig(), sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertFalse(playSession.stateValue.isAnimating)
        }
    }

    @Nested
    internal inner class Summary {
        @Test
        fun `GIVEN game ended WHEN clearSummary THEN showSummary becomes false`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertTrue(playSession.stateValue.showSummary)

            playSession.clearSummary()

            assertFalse(playSession.stateValue.showSummary)
        }
    }

    @Nested
    internal inner class EdgeCases {
        @Test
        fun `GIVEN session not started WHEN intent is accepted before session runs THEN intent is silently dropped`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ready, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN rapid clicks WHEN animations off THEN all are processed sequentially`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.b8))
            playSession.accept(PlayIntent.SelectSquare(Locus.c6))
            advanceUntilIdle()

            assertEquals(1, playSession.stateValue.results.size)
            assertTrue(playSession.stateValue.results.first().success)
        }

        @Test
        fun `GIVEN empty flow source WHEN run THEN ends gracefully`() = runTest {
            val emptySource = PlaySession.SessionsSource { flow {} }
            var completeCalled = false
            val playSession = buildPlaySession(
                sessions = emptySource,
                onPlayComplete = PlaySession.OnComplete { completeCalled = true },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
            assertTrue(completeCalled)
        }

        @Test
        fun `GIVEN promotion pending WHEN Promote intent THEN promotion completes`() = runTest {
            val puzzle = gameFactory.builder()
                .withId(1)
                .withTurn(Side.WHITE)
                .withPiece(Piece.King, Side.WHITE, Locus.e1)
                .withPiece(Piece.Pawn, Side.WHITE, Locus.g2)
                .withPiece(Piece.King, Side.BLACK, Locus.e8)
                .withPiece(Piece.Pawn, Side.BLACK, Locus.a2)
                .withRating(1200)
                .withMoves(listOf("g2g3", "a2a1"))
                .buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val session = BoardSession(
                navigation = NoOpNavigation,
                solution = NoOpSolution,
                opponent = ScriptedOpponent(puzzle),
            ).load(playable)
            val playSession = buildPlaySession(
                sessions = singleSessionSource(session),
                autoPromote = false,
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.a2, to = Locus.a1)

            assertNotNull(playSession.stateValue.boardState.promotion)

            playSession.accept(PlayIntent.Promote(Piece.Queen))
            advanceUntilIdle()

            assertNull(playSession.stateValue.boardState.promotion)
        }

        @Test
        fun `GIVEN odd-move puzzle WHEN opponent plays final move without animations THEN puzzle completes`() = runTest {
            val puzzle = buildPuzzle(moves = listOf("e2e4", "e7e5", "g1f3"))
            val session = buildSession(puzzle)
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)

            val state = playSession.stateValue
            assertEquals(PlaySessionState.Status.Ended, state.status)
            assertEquals(1, state.results.size)
            assertTrue(state.results.first().success)
        }

        @Test
        fun `GIVEN odd-move puzzle WHEN opponent plays final move with animations THEN puzzle completes`() = runTest {
            val puzzle = buildPuzzle(moves = listOf("e2e4", "e7e5", "g1f3"))
            val session = buildSession(puzzle)
            val config = animationsConfig(moveMs = 50).copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            advanceTimeBy(200)

            val state = playSession.stateValue
            assertEquals(PlaySessionState.Status.Ended, state.status)
            assertEquals(1, state.results.size)
            assertTrue(state.results.first().success)
        }

        @Test
        fun `GIVEN multiple intents queued during animation WHEN animation ends THEN only non-blocked are processed`() = runTest {
            val playSession = buildPlaySession(config = animationsConfig(moveMs = 100), sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceTimeBy(10)

            playSession.accept(PlayIntent.SelectSquare(Locus.b8))
            playSession.accept(PlayIntent.SelectSquare(Locus.c6))
            advanceTimeBy(500)

            assertFalse(playSession.stateValue.isAnimating)
            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN OnFirstFailure WHEN wrong move THEN showSummary is true`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertTrue(playSession.stateValue.showSummary)
            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN puzzle solved WHEN OnFirstFailure single session THEN ends and shows summary`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            val state = playSession.stateValue
            assertEquals(1, state.results.size)
            assertTrue(state.results.first().success)
        }
    }

    @Nested
    internal inner class IntentBlocking {
        @Test
        fun `GIVEN animating WHEN timer expires THEN game ends immediately`() = runTest {
            val config = animationsConfig(moveMs = 500).copy(
                timed = PlaySessionConfiguration.Timed(durationInMs = 200, mode = PlaySessionConfiguration.TimedMode.StartImmediately),
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceTimeBy(10)
            assertTrue(playSession.stateValue.isAnimating)

            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN animating WHEN Hint sent THEN it is blocked`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val config = animationsConfig(moveMs = 200).copy(hints = PlaySessionConfiguration.HintMode.Single)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceTimeBy(10)

            assertTrue(playSession.stateValue.isAnimating)
            playSession.accept(PlayIntent.Hint)
            advanceTimeBy(10)

            assertTrue(playSession.stateValue.hintAvailable)
        }

        @Test
        fun `GIVEN animating WHEN RequestAbandon sent THEN it is blocked`() = runTest {
            val playSession = buildPlaySession(config = animationsConfig(moveMs = 200), sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceTimeBy(10)

            playSession.accept(PlayIntent.RequestAbandon)
            advanceTimeBy(10)

            assertFalse(playSession.stateValue.abandonRequested)
        }
    }

    @Nested
    internal inner class EndModeBehavior {
        @Test
        fun `GIVEN OnFirstFailure WHEN puzzle fails THEN game ends immediately`() = runTest {
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnFirstFailure)
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
            assertTrue(playSession.stateValue.showSummary)
        }

        @Test
        fun `GIVEN OnSourceExhausted WHEN puzzle fails THEN continues`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
            assertEquals(1, playSession.stateValue.results.size)
            assertFalse(playSession.stateValue.results.first().success)
        }

        @Test
        fun `GIVEN OnFirstFailure with multiple sessions WHEN puzzle fails THEN onPlayComplete is called`() = runTest {
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnFirstFailure)
            var completedState: PlaySessionState? = null
            val playSession = buildPlaySession(
                config = config,
                sessions = multiSessionSource(3),
                onPlayComplete = PlaySession.OnComplete { completedState = it },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertNotNull(completedState)
            assertEquals(PlaySessionState.Status.Ended, completedState!!.status)
            assertEquals(1, completedState.results.size)
            assertFalse(completedState.results.first().success)
        }

        @Test
        fun `GIVEN OnSourceExhausted WHEN all puzzles solved THEN ends with Ended status`() = runTest {
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted, autoNextOverride = true)
            var completeCalled = false
            val playSession = buildPlaySession(
                config = config,
                sessions = singleSessionSource(),
                onPlayComplete = PlaySession.OnComplete { completeCalled = true },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertTrue(completeCalled)
        }
    }

    @Nested
    internal inner class ResultsIntegrity {
        @Test
        fun `GIVEN puzzle WHEN won THEN result contains correct id and rating`() = runTest {
            val puzzle = buildPuzzle(id = 42, rating = 1500)
            val session = buildSession(puzzle)
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            val result = playSession.stateValue.results.first()
            assertEquals(42, result.id)
            assertEquals(1500, result.rating)
            assertEquals(Outcome.Won, result.outcome)
        }

        @Test
        fun `GIVEN puzzle WHEN lost THEN result outcome is Lost`() = runTest {
            val puzzle = buildPuzzle(id = 7, rating = 800)
            val session = buildSession(puzzle)
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            val result = playSession.stateValue.results.first()
            assertEquals(7, result.id)
            assertEquals(800, result.rating)
            assertFalse(result.success)
        }

        @Test
        fun `GIVEN timed session WHEN timer expires mid-puzzle THEN no result is added`() = runTest {
            val puzzle = buildPuzzle()
            val session = buildSession(puzzle)
            val config = timedConfig(durationMs = 200L, mode = PlaySessionConfiguration.TimedMode.StartImmediately)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertTrue(playSession.stateValue.results.isEmpty())
        }

        @Test
        fun `GIVEN timed session WHEN puzzle solved before timer expires THEN solved puzzle IS in results`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
                timed = PlaySessionConfiguration.Timed(durationInMs = 2000, mode = PlaySessionConfiguration.TimedMode.StartOnClick),
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            // Solve puzzle 1
            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            // Let timer expire on puzzle 2
            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            val results = playSession.stateValue.results
            assertEquals(1, results.size)
            assertTrue(results.first().success)
            assertEquals(1, results.first().id)
        }
    }

    @Nested
    internal inner class Concurrency {
        @Test
        fun `GIVEN many rapid intents WHEN all processed THEN state is consistent`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            repeat(50) {
                playSession.accept(PlayIntent.SelectSquare(Locus.e7))
                playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            }
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN session started WHEN intent is accepted after channel is closed THEN no crash`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
        }
    }

    @Nested
    internal inner class Navigation {
        @Test
        fun `GIVEN no navigation configured WHEN state read THEN navigation is null`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertNull(playSession.stateValue.navigation)
        }

        @Test
        fun `GIVEN session with pre-loaded moves WHEN run THEN navigation reflects history`() = runTest {
            val game = gameFactory.builder().withDefaultBoard().buildGame()
            game.start()
            game.play(Locus.e2, Locus.e4)
            game.play(Locus.e7, Locus.e5)
            val session = BoardSession(
                navigation = GameNavigation(game),
                opponent = NoOpOpponent,
            ).load(GamePlayableBoard(game, Side.WHITE))
            val playSession = buildPlaySession(sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            val navigation = playSession.stateValue.navigation
            assertNotNull(navigation)
            assertTrue(navigation!!.canGoBack)
            assertFalse(navigation.canGoForward)
        }

        @Test
        fun `GIVEN session with no moves WHEN run THEN navigation has no back`() = runTest {
            val game = gameFactory.builder().withDefaultBoard().buildGame()
            game.start()
            val session = BoardSession(
                navigation = GameNavigation(game),
                opponent = NoOpOpponent,
            ).load(GamePlayableBoard(game, Side.WHITE))
            val playSession = buildPlaySession(sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            val navigation = playSession.stateValue.navigation
            assertNotNull(navigation)
            assertFalse(navigation!!.canGoBack)
            assertFalse(navigation.canGoForward)
        }

        @Test
        fun `GIVEN Navigate ToStart WHEN accepted THEN intent is processed`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.Navigate(PlayIntent.Navigation.ToStart))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN Navigate Back WHEN accepted THEN state continues`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.Navigate(PlayIntent.Navigation.Back))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN Navigate Forward WHEN accepted THEN state continues`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.Navigate(PlayIntent.Navigation.Forward))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN Navigate ToEnd WHEN accepted THEN state continues`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.Navigate(PlayIntent.Navigation.ToEnd))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }
    }

    @Nested
    internal inner class StateConsistency {
        @Test
        fun `GIVEN session WHEN selecting then invalid target THEN selection is cleared`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            assertEquals(Locus.e7, playSession.stateValue.boardState.boardData.selection)

            playSession.accept(PlayIntent.SelectSquare(Locus.d7))
            advanceUntilIdle()

            assertNull(playSession.stateValue.boardState.boardData.selection)
        }

        @Test
        fun `GIVEN session WHEN selecting same square twice THEN deselects`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()

            assertNull(playSession.stateValue.boardState.boardData.selection)
        }

        @Test
        fun `GIVEN puzzle failed WHEN result checked THEN rating is preserved`() = runTest {
            val session = buildSession(buildPuzzle(rating = 1800, id = 99))
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            val result = playSession.stateValue.results.first()
            assertEquals(99, result.id)
            assertEquals(1800, result.rating)
        }

        @Test
        fun `GIVEN puzzles solved WHEN results list THEN maintains order`() = runTest {
            val puzzle1 = buildPuzzle(id = 1, rating = 1000)
            val puzzle2 = buildPuzzle(id = 2, rating = 1200)
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(buildSession(puzzle1), buildSession(puzzle2)))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            val results = playSession.stateValue.results
            assertEquals(2, results.size)
            assertEquals(1, results[0].id)
            assertEquals(2, results[1].id)
            assertEquals(1000, results[0].rating)
            assertEquals(1200, results[1].rating)
        }

        @Test
        fun `GIVEN game ended WHEN state checked THEN board is cleared`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
            assertFalse(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN config with hints WHEN run THEN hints available`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val config = noAnimationsConfig().copy(hints = PlaySessionConfiguration.HintMode.Unlimited)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertTrue(playSession.stateValue.hintAvailable)
        }

        @Test
        fun `GIVEN session in Ready WHEN intent arrives THEN transitions to Playing`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            assertEquals(PlaySessionState.Status.Ready, playSession.stateValue.status)

            playSession.accept(PlayIntent.SelectSquare(Locus.a7))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN abandon dismissed WHEN playing continues THEN moves still work`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.RequestAbandon)
            advanceUntilIdle()
            playSession.accept(PlayIntent.DismissAbandon)
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)

            assertTrue(playSession.stateValue.boardState.movePlayed)
        }

        @Test
        fun `GIVEN timer not started WHEN no moves made THEN time stays at initial value`() = runTest {
            val config = timedConfig(durationMs = 5000L, mode = PlaySessionConfiguration.TimedMode.StartOnClick)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceTimeBy(1000)

            assertEquals(5000L, playSession.stateValue.remainingTimeMs)
        }

        @Test
        fun `GIVEN mixed results WHEN game ends THEN all results present`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(3))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            // Solve puzzle 1 correctly
            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            // Fail puzzle 2
            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            // Solve puzzle 3 correctly
            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            val results = playSession.stateValue.results
            assertEquals(3, results.size)
            assertTrue(results[0].success)
            assertFalse(results[1].success)
            assertTrue(results[2].success)
        }

        @Test
        fun `GIVEN animation during board swap WHEN second session THEN first session does not animate`() = runTest {
            val config = animationsConfig(boardSwapMs = 0, moveMs = 0)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertFalse(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN deselection WHEN clicking invalid target THEN no move played`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e3)

            val board = playSession.stateValue.boardState
            assertFalse(board.movePlayed)
        }

        @Test
        fun `GIVEN complete session WHEN no more puzzles THEN onPlayComplete called with correct results`() = runTest {
            val session = buildSession(buildPuzzle(id = 5, rating = 1400))
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            var finalState: PlaySessionState? = null
            val playSession = buildPlaySession(
                config = config,
                sessions = singleSessionSource(session),
                onPlayComplete = PlaySession.OnComplete { finalState = it },
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertNotNull(finalState)
            assertEquals(1, finalState!!.results.size)
            assertTrue(finalState.results.first().success)
            assertEquals(5, finalState.results.first().id)
        }

        @Test
        fun `GIVEN paused session WHEN timer still active THEN timer keeps counting`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = false,
                timed = PlaySessionConfiguration.Timed(
                    durationInMs = 5000,
                    mode = PlaySessionConfiguration.TimedMode.StartOnClick,
                ),
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(PlaySessionState.Status.Paused, playSession.stateValue.status)
            timer.emit(CountdownTimer.Remainder(4, 500))
            advanceUntilIdle()

            val timeAfterPause = playSession.stateValue.remainingTimeMs
            assertNotNull(timeAfterPause)
            assertTrue(timeAfterPause!! < 5000L)
        }

        @Test
        fun `GIVEN session with different ratings WHEN multiple puzzles played THEN each result has correct rating`() = runTest {
            val session1 = buildSession(buildPuzzle(id = 1, rating = 800))
            val session2 = buildSession(buildPuzzle(id = 2, rating = 2000))
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = true,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(session1, session2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            // Fail puzzle 1
            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            // Solve puzzle 2
            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            val results = playSession.stateValue.results
            assertEquals(2, results.size)
            assertEquals(800, results[0].rating)
            assertEquals(2000, results[1].rating)
            assertFalse(results[0].success)
            assertTrue(results[1].success)
        }
    }

    @Nested
    internal inner class ClearSummaryAndReConfig {
        @Test
        fun `GIVEN ended session WHEN clearSummary THEN showSummary becomes false`() = runTest {
            val config = noAnimationsConfig().copy(endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertTrue(playSession.stateValue.showSummary)
            playSession.clearSummary()
            assertFalse(playSession.stateValue.showSummary)
        }

        @Test
        fun `GIVEN play session WHEN autoNext disabled THEN pauses between sessions`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource(), autoNext = false)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()

            assertNotNull(playSession.stateValue.boardState.boardData.selection)
        }

        @Test
        fun `GIVEN running session WHEN autoNext toggled off THEN next session pauses`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            settingsRepository.setAppSettings(
                settingsRepository.getCurrentSettings().copy(autoNextPuzzle = false)
            )

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(PlaySessionState.Status.Paused, playSession.stateValue.status)
        }
    }

    @Nested
    internal inner class TimerStartImmediately {
        @Test
        fun `GIVEN StartImmediately timer WHEN run THEN timer starts without click`() = runTest {
            val config = noAnimationsConfig().copy(
                timed = PlaySessionConfiguration.Timed(
                    durationInMs = 3000,
                    mode = PlaySessionConfiguration.TimedMode.StartImmediately,
                ),
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            timer.emit(CountdownTimer.Remainder(2, 500))
            advanceUntilIdle()

            val remaining = playSession.stateValue.remainingTimeMs
            assertNotNull(remaining)
            assertTrue(remaining!! < 3000L)
        }

        @Test
        fun `GIVEN StartImmediately timer WHEN expires THEN session ends`() = runTest {
            val config = noAnimationsConfig().copy(
                timed = PlaySessionConfiguration.Timed(
                    durationInMs = 1000,
                    mode = PlaySessionConfiguration.TimedMode.StartImmediately,
                ),
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()
            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN StartOnClick timer WHEN no click made THEN time stays full`() = runTest {
            val config = noAnimationsConfig().copy(
                timed = PlaySessionConfiguration.Timed(
                    durationInMs = 5000,
                    mode = PlaySessionConfiguration.TimedMode.StartOnClick,
                ),
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertEquals(5000L, playSession.stateValue.remainingTimeMs)
        }
    }

    @Nested
    internal inner class AnimationEdgeCases {
        @Test
        fun `GIVEN animation in progress WHEN SelectSquare sent THEN intent is dropped`() = runTest {
            val config = animationsConfig(moveMs = 200L)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)

            assertTrue(playSession.stateValue.isAnimating)

            playSession.accept(PlayIntent.SelectSquare(Locus.d7))
            advanceUntilIdle()

            assertTrue(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN animation in progress WHEN Hint sent THEN hint is dropped`() = runTest {
            val config = animationsConfig(moveMs = 200L).copy(
                hints = PlaySessionConfiguration.HintMode.Unlimited,
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)

            assertTrue(playSession.stateValue.isAnimating)
            playSession.accept(PlayIntent.Hint)
            advanceUntilIdle()
            assertTrue(playSession.stateValue.isAnimating)
        }

        @Test
        fun `GIVEN first session WHEN animations enabled THEN first session skips board swap`() = runTest {
            val config = animationsConfig(boardSwapMs = 200L, moveMs = 100L)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceTimeBy(50)

            assertFalse(playSession.stateValue.isAnimating)
            assertEquals(PlaySessionState.Status.Ready, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN timer expires WHEN animation ongoing THEN animation is cancelled and session ends`() = runTest {
            val config = animationsConfig(moveMs = 500L).copy(
                timed = PlaySessionConfiguration.Timed(
                    durationInMs = 800,
                    mode = PlaySessionConfiguration.TimedMode.StartOnClick,
                ),
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)

            assertTrue(playSession.stateValue.isAnimating)

            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()
            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
            assertFalse(playSession.stateValue.isAnimating)
        }
    }

    @Nested
    internal inner class AcceptBeforeRun {
        @Test
        fun `GIVEN session not yet run WHEN accept called THEN intent is silently dropped`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))

            assertEquals(PlaySessionState.Status.Loading, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN session ended WHEN accept called THEN intent is silently dropped`() = runTest {
            val playSession = buildPlaySession(sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.d7, to = Locus.d5)

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)

            playSession.accept(PlayIntent.SelectSquare(Locus.a2))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }
    }

    @Nested
    internal inner class PausedState {
        @Test
        fun `GIVEN paused session WHEN non-resumable intent sent THEN it is skipped`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = false,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(PlaySessionState.Status.Paused, playSession.stateValue.status)

            playSession.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            playSession.accept(PlayIntent.Hint)
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Paused, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN paused session WHEN Resume sent THEN session transitions to Playing`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = false,
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(PlaySessionState.Status.Paused, playSession.stateValue.status)

            playSession.accept(PlayIntent.Resume)
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Playing, playSession.stateValue.status)
        }

        @Test
        fun `GIVEN paused session WHEN timer expires THEN session ends`() = runTest {
            val config = noAnimationsConfig().copy(
                endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
                autoNextOverride = false,
                timed = PlaySessionConfiguration.Timed(
                    durationInMs = 2000,
                    mode = PlaySessionConfiguration.TimedMode.StartOnClick,
                ),
            )
            val playSession = buildPlaySession(config = config, sessions = multiSessionSource(2), timer = timer)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            makeMove(playSession, from = Locus.e7, to = Locus.e5)
            makeMove(playSession, from = Locus.b8, to = Locus.c6)

            assertEquals(PlaySessionState.Status.Paused, playSession.stateValue.status)

            timer.emit(CountdownTimer.Remainder(0, 0))
            advanceUntilIdle()

            assertEquals(PlaySessionState.Status.Ended, playSession.stateValue.status)
        }
    }

    @Nested
    internal inner class HintEdgeCases {
        @Test
        fun `GIVEN single hint mode WHEN hint used THEN hintAvailable becomes false`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val config = noAnimationsConfig().copy(
                hints = PlaySessionConfiguration.HintMode.Single,
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertTrue(playSession.stateValue.hintAvailable)

            playSession.accept(PlayIntent.Hint)
            advanceUntilIdle()

            assertFalse(playSession.stateValue.hintAvailable)
        }

        @Test
        fun `GIVEN unlimited hint mode WHEN hint used THEN hintAvailable remains true`() = runTest {
            val session = buildSessionWithSolution(buildPuzzle())
            val config = noAnimationsConfig().copy(
                hints = PlaySessionConfiguration.HintMode.Unlimited,
            )
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource(session))

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertTrue(playSession.stateValue.hintAvailable)

            playSession.accept(PlayIntent.Hint)
            advanceUntilIdle()

            assertTrue(playSession.stateValue.hintAvailable)
        }

        @Test
        fun `GIVEN no hint mode WHEN created THEN hintAvailable is false`() = runTest {
            val config = noAnimationsConfig().copy(hints = null)
            val playSession = buildPlaySession(config = config, sessions = singleSessionSource())

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { playSession.run() }
            advanceUntilIdle()

            assertFalse(playSession.stateValue.hintAvailable)
        }
    }

    private fun buildPuzzle(rating: Int = 1200, id: Int? = 1, moves: List<String> = listOf("e2e4", "e7e5", "g1f3", "b8c6")): Puzzle =
        gameFactory.builder()
            .withId(id)
            .withDefaultBoard()
            .withRating(rating)
            .withMoves(moves)
            .buildPuzzle()

    private fun buildSession(puzzle: Puzzle): BoardSession = BoardSession(
        navigation = NoOpNavigation,
        solution = NoOpSolution,
        opponent = ScriptedOpponent(puzzle),
    ).load(PuzzlePlayableBoard(puzzle))

    private fun buildSession(id: Int): BoardSession = buildSession(buildPuzzle(id = id))

    private fun buildSessionWithSolution(puzzle: Puzzle): BoardSession = BoardSession(
        navigation = NoOpNavigation,
        solution = PuzzleSolution(puzzle),
        opponent = ScriptedOpponent(puzzle),
    ).load(PuzzlePlayableBoard(puzzle))

    private fun noAnimationsConfig(): PlaySessionConfiguration = PlaySessionConfiguration(
        endMode = PlaySessionConfiguration.EndMode.OnFirstFailure,
        moveAnimationMs = 0,
        boardSwapAnimationMs = 0,
        solutionStepDelayMs = 0,
    )

    private fun animationsConfig(moveMs: Long = 50L, boardSwapMs: Long = 50L, solutionMs: Long = 50L): PlaySessionConfiguration =
        PlaySessionConfiguration(
            endMode = PlaySessionConfiguration.EndMode.OnFirstFailure,
            moveAnimationMs = moveMs,
            boardSwapAnimationMs = boardSwapMs,
            solutionStepDelayMs = solutionMs,
        )

    private fun timedConfig(
        durationMs: Long = 3000L,
        mode: PlaySessionConfiguration.TimedMode = PlaySessionConfiguration.TimedMode.StartOnClick,
    ): PlaySessionConfiguration = noAnimationsConfig().copy(
        timed = PlaySessionConfiguration.Timed(durationInMs = durationMs, mode = mode),
    )

    private fun singleSessionSource(session: BoardSession): PlaySession.SessionsSource =
        PlaySession.SessionsSource { flowOf(session) }

    private fun singleSessionSource(): PlaySession.SessionsSource =
        PlaySession.SessionsSource { flowOf(buildSession(buildPuzzle())) }

    private fun multiSessionSource(vararg sessions: BoardSession): PlaySession.SessionsSource =
        PlaySession.SessionsSource { flow { sessions.forEach { emit(it) } } }

    private fun multiSessionSource(count: Int): PlaySession.SessionsSource =
        PlaySession.SessionsSource { (0 until count).asFlow().map { buildSession(it + 1) } }

    private fun controllableSessionSource(flow: Flow<BoardSession>): PlaySession.SessionsSource =
        PlaySession.SessionsSource { flow }

    private fun buildPlaySession(
        config: PlaySessionConfiguration = noAnimationsConfig(),
        sessions: PlaySession.SessionsSource,
        timer: CountdownTimer? = null,
        onPlayComplete: PlaySession.OnComplete = PlaySession.OnComplete {},
        onSessionComplete: PlaySession.OnComplete = PlaySession.OnComplete {},
        enableAnimations: Boolean? = null,
        autoPromote: Boolean = true,
        autoNext: Boolean = true,
    ): PlaySession {
        val animationsEnabled = enableAnimations
            ?: (config.moveAnimationMs > 0 || config.boardSwapAnimationMs > 0 || config.solutionStepDelayMs > 0)
        settingsRepository.setAppSettings(
            settingsRepository.getCurrentSettings().copy(
                autoNextPuzzle = autoNext,
                enableAnimations = animationsEnabled,
                autoPromote = autoPromote,
            )
        )
        return PlaySession(
            settingsRepository = settingsRepository,
            config = config,
            sessions = sessions,
            timer = timer,
            onPlayComplete = onPlayComplete,
            onSessionComplete = onSessionComplete,
        )
    }

    private fun TestScope.makeMove(playSession: PlaySession, from: Locus, to: Locus) {
        playSession.accept(PlayIntent.SelectSquare(from))
        advanceUntilIdle()
        playSession.accept(PlayIntent.SelectSquare(to))
        advanceUntilIdle()
    }
}
