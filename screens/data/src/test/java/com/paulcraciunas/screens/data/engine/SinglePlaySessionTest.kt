package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.GameNavigation
import com.paulcraciunas.screens.data.GamePlayableBoard
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.NoOpOpponent
import com.paulcraciunas.screens.data.NoOpSolution
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.ScriptedOpponent
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
internal class SinglePlaySessionTest {
    private val gameFactory = RealGameFactory()
    private val settingsRepository = FakeAppSettingsRepository.default()

    @Nested
    internal inner class Initialization {
        @Test
        fun `GIVEN single session WHEN created THEN state is Loading`() = runTest {
            val session = buildSinglePlaySession()

            assertEquals(SingleSessionState.Status.Loading, session.state.value.status)
        }

        @Test
        fun `GIVEN single session WHEN run THEN state transitions to Ready`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.Ready, session.state.value.status)
        }

        @Test
        fun `GIVEN single session WHEN run THEN board state is populated`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            val boardState = session.state.value.boardState
            assertEquals(Side.WHITE, boardState.player)
        }

        @Test
        fun `GIVEN single session WHEN run THEN navigation is populated`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            assertNotNull(session.state.value.navigation)
        }

        @Test
        fun `GIVEN session with pre-loaded moves WHEN run THEN navigation reflects history`() = runTest {
            val game = gameFactory.builder().withDefaultBoard().buildGame()
            game.start()
            game.play(Locus.e2, Locus.e4)
            game.play(Locus.e7, Locus.e5)
            val boardSession = BoardSession(
                navigation = GameNavigation(game),
                opponent = NoOpOpponent,
            ).load(GamePlayableBoard(game, Side.WHITE))
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            val navigation = session.state.value.navigation
            assertNotNull(navigation)
            assertTrue(navigation!!.canGoBack)
            assertFalse(navigation.canGoForward)
        }

        @Test
        fun `GIVEN session without navigation WHEN run THEN navigation is null`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildPuzzleSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            assertNull(session.state.value.navigation)
        }

        @Test
        fun `GIVEN single session WHEN run with no animations THEN isAnimating is false`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            assertFalse(session.state.value.isAnimating)
        }

        @Test
        fun `GIVEN session WHEN run THEN loads autoPromote setting`() = runTest {
            val boardSession = buildGameSession()
            val session = buildSinglePlaySession(autoPromote = false)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            assertFalse(boardSession.autoPromote)
        }
    }

    @Nested
    internal inner class PlayerMoves {
        @Test
        fun `GIVEN Ready state WHEN first move THEN status transitions to Playing`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e2))
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.Playing, session.state.value.status)
        }

        @Test
        fun `GIVEN playing WHEN selecting own piece THEN board shows selection`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e2))
            advanceUntilIdle()

            assertEquals(Locus.e2, session.state.value.boardState.boardData.selection)
        }

        @Test
        fun `GIVEN piece selected WHEN valid move THEN move is played`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.e2, to = Locus.e4)

            assertTrue(session.state.value.boardState.movePlayed)
        }

        @Test
        fun `GIVEN clicking empty square WHEN no selection THEN nothing happens`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e4))
            advanceUntilIdle()

            assertNull(session.state.value.boardState.boardData.selection)
        }

        @Test
        fun `GIVEN clicking opponent piece WHEN no selection THEN nothing happens`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()

            assertNull(session.state.value.boardState.boardData.selection)
        }
    }

    @Nested
    internal inner class GameOverWithTerminate {
        @Test
        fun `GIVEN Terminate mode WHEN puzzle solved THEN run completes`() = runTest {
            val config = noAnimationsConfig().copy(
                gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.Terminate
            )
            val session = buildSinglePlaySession(config)
            val boardSession = buildPuzzleSession()

            var runCompleted = false
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
                runCompleted = true
            }
            advanceUntilIdle()

            // Puzzle moves: e2e4 (opponent), e7e5 (player), g1f3 (opponent), b8c6 (player)
            makeMove(session, from = Locus.e7, to = Locus.e5)
            makeMove(session, from = Locus.b8, to = Locus.c6)

            assertTrue(runCompleted)
        }

        @Test
        fun `GIVEN Terminate mode WHEN wrong move THEN run completes`() = runTest {
            val config = noAnimationsConfig().copy(
                gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.Terminate
            )
            val session = buildSinglePlaySession(config)
            val boardSession = buildPuzzleSession()

            var runCompleted = false
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
                runCompleted = true
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d7, to = Locus.d5)

            assertTrue(runCompleted)
        }
    }

    @Nested
    internal inner class GameOverWithAllowNavigation {
        @Test
        fun `GIVEN AllowNavigation WHEN game ends THEN status is GameOver`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            // Play the checkmating move (Qd8-h4#, fool's mate)
            makeMove(session, from = Locus.d8, to = Locus.h4)

            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)
        }

        @Test
        fun `GIVEN AllowNavigation WHEN game ends THEN outcome is set`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d8, to = Locus.h4)

            assertEquals(Outcome.Won, session.state.value.boardState.outcome)
        }

        @Test
        fun `GIVEN GameOver WHEN navigate back THEN navigation works`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d8, to = Locus.h4)
            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)

            session.accept(PlayIntent.Navigate(PlayIntent.Navigation.Back))
            advanceUntilIdle()

            val nav = session.state.value.navigation
            assertNotNull(nav)
            assertTrue(nav!!.canGoForward)
        }

        @Test
        fun `GIVEN GameOver WHEN navigate to start THEN all moves undone`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d8, to = Locus.h4)
            session.accept(PlayIntent.Navigate(PlayIntent.Navigation.ToStart))
            advanceUntilIdle()

            val nav = session.state.value.navigation
            assertNotNull(nav)
            assertFalse(nav!!.canGoBack)
            assertTrue(nav.canGoForward)
        }

        @Test
        fun `GIVEN GameOver WHEN click piece THEN ignored`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d8, to = Locus.h4)

            session.accept(PlayIntent.SelectSquare(Locus.e2))
            advanceUntilIdle()

            assertNull(session.state.value.boardState.boardData.selection)
            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)
        }

        @Test
        fun `GIVEN GameOver WHEN hint sent THEN ignored`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d8, to = Locus.h4)

            session.accept(PlayIntent.Hint)
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)
        }

        @Test
        fun `GIVEN AllowNavigation WHEN game ends THEN session stays alive`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            var runCompleted = false
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
                runCompleted = true
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d8, to = Locus.h4)

            assertFalse(runCompleted)
            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)
        }

        @Test
        fun `GIVEN GameOver WHEN navigate back THEN status returns to Playing`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d8, to = Locus.h4)
            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)

            session.accept(PlayIntent.Navigate(PlayIntent.Navigation.Back))
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.Playing, session.state.value.status)
        }

        @Test
        fun `GIVEN navigated back from GameOver WHEN new move played THEN move succeeds`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d8, to = Locus.h4)
            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)

            session.accept(PlayIntent.Navigate(PlayIntent.Navigation.Back))
            advanceUntilIdle()

            // Now try a different move (Black queen from d8 after undo)
            makeMove(session, from = Locus.d8, to = Locus.e7)

            assertTrue(session.state.value.boardState.movePlayed)
            assertEquals(SingleSessionState.Status.Playing, session.state.value.status)
        }

        @Test
        fun `GIVEN navigated back from GameOver WHEN jump to end THEN GameOver again`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildCheckmateSession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.d8, to = Locus.h4)
            session.accept(PlayIntent.Navigate(PlayIntent.Navigation.Back))
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.Playing, session.state.value.status)

            session.accept(PlayIntent.Navigate(PlayIntent.Navigation.ToEnd))
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)
        }
    }

    @Nested
    internal inner class Navigation {
        @Test
        fun `GIVEN navigable session WHEN move played THEN canGoBack is true`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.e2, to = Locus.e4)

            val nav = session.state.value.navigation
            assertNotNull(nav)
            assertTrue(nav!!.canGoBack)
        }

        @Test
        fun `GIVEN move played WHEN navigate back THEN board reverts`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.e2, to = Locus.e4)

            session.accept(PlayIntent.Navigate(PlayIntent.Navigation.Back))
            advanceUntilIdle()

            val nav = session.state.value.navigation
            assertNotNull(nav)
            assertTrue(nav!!.canGoForward)
        }

        @Test
        fun `GIVEN navigated back WHEN navigate forward THEN board restores`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.e2, to = Locus.e4)

            session.accept(PlayIntent.Navigate(PlayIntent.Navigation.Back))
            advanceUntilIdle()
            session.accept(PlayIntent.Navigate(PlayIntent.Navigation.Forward))
            advanceUntilIdle()

            val nav = session.state.value.navigation
            assertNotNull(nav)
            assertTrue(nav!!.canGoBack)
            assertFalse(nav.canGoForward)
        }
    }

    @Nested
    internal inner class Animations {
        @Test
        fun `GIVEN animations enabled WHEN move played THEN isAnimating becomes true`() = runTest {
            val config = animationsConfig()
            val session = buildSinglePlaySession(config)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.e2, to = Locus.e4)

            assertTrue(session.state.value.isAnimating)
        }

        @Test
        fun `GIVEN animating WHEN animation completes THEN isAnimating becomes false`() = runTest {
            val config = animationsConfig(moveMs = 50)
            val session = buildSinglePlaySession(config)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e2))
            advanceUntilIdle()
            session.accept(PlayIntent.SelectSquare(Locus.e4))
            advanceTimeBy(100)

            assertFalse(session.state.value.isAnimating)
        }

        @Test
        fun `GIVEN animating WHEN user clicks THEN click is ignored`() = runTest {
            val config = animationsConfig(moveMs = 100)
            val session = buildSinglePlaySession(config)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e2))
            advanceUntilIdle()
            session.accept(PlayIntent.SelectSquare(Locus.e4))
            advanceTimeBy(10)

            assertTrue(session.state.value.isAnimating)
            session.accept(PlayIntent.SelectSquare(Locus.d2))
            advanceTimeBy(10)

            assertNull(session.state.value.boardState.boardData.selection)
        }
    }

    @Nested
    internal inner class Abandon {
        @Test
        fun `GIVEN playing WHEN RequestAbandon THEN abandonRequested is true`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e2))
            advanceUntilIdle()
            session.accept(PlayIntent.RequestAbandon)
            advanceUntilIdle()

            assertTrue(session.state.value.abandonRequested)
        }

        @Test
        fun `GIVEN abandon requested WHEN DismissAbandon THEN abandonRequested is false`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            session.accept(PlayIntent.RequestAbandon)
            advanceUntilIdle()
            session.accept(PlayIntent.DismissAbandon)
            advanceUntilIdle()

            assertFalse(session.state.value.abandonRequested)
        }

        @Test
        fun `GIVEN AllowNavigation WHEN ConfirmAbandon THEN status is GameOver`() = runTest {
            val session = buildSinglePlaySession()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e2))
            advanceUntilIdle()
            session.accept(PlayIntent.ConfirmAbandon)
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)
        }

        @Test
        fun `GIVEN Terminate WHEN ConfirmAbandon THEN run completes`() = runTest {
            val config = noAnimationsConfig().copy(
                gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.Terminate
            )
            val session = buildSinglePlaySession(config)

            var runCompleted = false
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(buildGameSession())
                runCompleted = true
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e2))
            advanceUntilIdle()
            session.accept(PlayIntent.ConfirmAbandon)
            advanceUntilIdle()

            assertTrue(runCompleted)
        }
    }

    @Nested
    internal inner class ErrorHandling {
        @Test
        fun `GIVEN opponent throws WHEN move played THEN status is Failed`() = runTest {
            val session = buildSinglePlaySession()
            val opponent = object : com.paulcraciunas.screens.data.OpponentStrategy {
                override fun canPlay(): Boolean = true
                override suspend fun init() {}
                override suspend fun playNext(): Boolean { throw RuntimeException("Engine crash") }
                override suspend fun shutdown() {}
            }
            val boardSession = BoardSession(
                navigation = GameNavigation(gameFactory.builder().withDefaultBoard().buildGame().also { it.start() }),
                opponent = opponent,
            ).load(
                GamePlayableBoard(
                    gameFactory.builder().withDefaultBoard().buildGame().also { it.start() },
                    Side.WHITE
                )
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            makeMove(session, from = Locus.e2, to = Locus.e4)

            assertEquals(SingleSessionState.Status.Failed, session.state.value.status)
            assertEquals("Engine crash", session.state.value.errorMessage)
        }
    }

    @Nested
    internal inner class Cancellation {
        @Test
        fun `GIVEN session running WHEN run is cancelled THEN state is GameOver with no animation`() = runTest {
            val session = buildSinglePlaySession()
            val boardSession = buildPuzzleSession()

            val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.Ready, session.state.value.status)

            job.cancel()
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)
            assertFalse(session.state.value.isAnimating)
        }

        @Test
        fun `GIVEN animation in progress WHEN run is cancelled THEN isAnimating is false`() = runTest {
            val session = buildSinglePlaySession(config = animationsConfig(moveMs = 200))
            val boardSession = buildPuzzleSession()

            val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                session.run(boardSession)
            }
            advanceUntilIdle()

            session.accept(PlayIntent.SelectSquare(Locus.e7))
            advanceUntilIdle()
            session.accept(PlayIntent.SelectSquare(Locus.e5))
            advanceTimeBy(10)

            assertTrue(session.state.value.isAnimating)

            job.cancel()
            advanceUntilIdle()

            assertEquals(SingleSessionState.Status.GameOver, session.state.value.status)
            assertFalse(session.state.value.isAnimating)
        }
    }

    // -- Helpers --

    private fun buildCheckmateSession(): BoardSession {
        // Scholar's mate position: White has Qh4, can play Qf2# (or Qxf7#)
        // Use a FEN where white queen on h4 can deliver mate on f2
        // Simpler: set up fool's mate - after f3, e5, g4: Black Qh4#
        // Position after 1.f3 e5 2.g4 — Black to play Qh4#
        val game = gameFactory.builder().withDefaultBoard().buildGame()
        game.start()
        game.play(Locus.f2, Locus.f3) // 1. f3
        game.play(Locus.e7, Locus.e5) // 1... e5
        game.play(Locus.g2, Locus.g4) // 2. g4
        // Black to play Qh4# (fool's mate)
        return BoardSession(
            navigation = GameNavigation(game),
            opponent = NoOpOpponent,
        ).load(GamePlayableBoard(game, Side.BLACK))
    }

    private fun buildGameSession(): BoardSession {
        val game = gameFactory.builder().withDefaultBoard().buildGame()
        game.start()
        return BoardSession(
            navigation = GameNavigation(game),
            opponent = NoOpOpponent,
        ).load(GamePlayableBoard(game, Side.WHITE))
    }

    private fun buildPuzzleSession(): BoardSession {
        val puzzle = gameFactory.builder()
            .withDefaultBoard()
            .withRating(1200)
            .withMoves(listOf("e2e4", "e7e5", "g1f3", "b8c6"))
            .buildPuzzle()
        return BoardSession(
            navigation = NoOpNavigation,
            solution = NoOpSolution,
            opponent = ScriptedOpponent(puzzle),
        ).load(PuzzlePlayableBoard(puzzle))
    }

    private fun noAnimationsConfig(): SingleSessionConfiguration = SingleSessionConfiguration(
        gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.AllowNavigation,
        moveAnimationMs = 0,
        boardSwapAnimationMs = 0,
    )

    private fun animationsConfig(moveMs: Long = 50L): SingleSessionConfiguration =
        SingleSessionConfiguration(
            gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.AllowNavigation,
            moveAnimationMs = moveMs,
            boardSwapAnimationMs = moveMs,
        )

    private fun buildSinglePlaySession(
        config: SingleSessionConfiguration = noAnimationsConfig(),
        autoPromote: Boolean = true,
    ): SinglePlaySession {
        settingsRepository.setAppSettings(
            settingsRepository.getCurrentSettings().copy(
                enableAnimations = config.moveAnimationMs > 0 || config.boardSwapAnimationMs > 0,
                autoPromote = autoPromote,
            )
        )
        return SinglePlaySession(
            settingsRepository = settingsRepository,
            config = config,
        )
    }

    private fun TestScope.makeMove(session: SinglePlaySession, from: Locus, to: Locus) {
        session.accept(PlayIntent.SelectSquare(from))
        advanceUntilIdle()
        session.accept(PlayIntent.SelectSquare(to))
        advanceUntilIdle()
    }
}
