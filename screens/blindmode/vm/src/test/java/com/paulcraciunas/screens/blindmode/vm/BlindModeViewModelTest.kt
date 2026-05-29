package com.paulcraciunas.screens.blindmode.vm

import com.paulcraciunas.domain.api.blindmode.FakeOnBlindModeGameComplete
import com.paulcraciunas.domain.api.general.FakeTimer
import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.domain.impl.blindmode.BlindModeOrchestratorImpl
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.FakeChessEngine
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.screens.common.controls.SideSelection
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.FakeUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
internal class BlindModeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val engine = FakeChessEngine()
    private val appSettings = FakeAppSettingsRepository()
    private val userRepository = FakeUserRepository()
    private val fakeOnComplete = FakeOnBlindModeGameComplete()
    private val timer = FakeTimer()

    private lateinit var underTest: BlindModeViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = BlindModeViewModel(
            orchestrator = BlindModeOrchestratorImpl(
                chessEngine = engine,
                serializer = FenSerializer(RealGameFactory()),
                dispatcher = testDispatcher,
            ),
            onComplete = fakeOnComplete,
            timer = timer,
            randomFactory = FixedRandomFactory(),
            appSettingsRepository = appSettings,
            userRepository = userRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN initial state WHEN created THEN uiState is Setup with defaults`() {
        val state = underTest.uiState.value as BlindModeUiState.Setup
        assertTrue(state.isTrainingMode)
        assertEquals(SideSelection.WHITE, state.selectedSide)
    }

    @Nested
    internal inner class SetupInteractions {
        @Test
        fun `GIVEN setup WHEN onTrainingModeToggled false THEN isTrainingMode is false`() {
            underTest.onTrainingModeToggled(false)

            val state = underTest.uiState.value as BlindModeUiState.Setup
            assertFalse(state.isTrainingMode)
        }

        @Test
        fun `GIVEN setup WHEN onSideSelected Black THEN selectedSide updates`() {
            underTest.onSideSelected(SideSelection.BLACK)

            val state = underTest.uiState.value as BlindModeUiState.Setup
            assertEquals(SideSelection.BLACK, state.selectedSide)
        }

        @Test
        fun `GIVEN setup WHEN onSideSelected Random THEN selectedSide is random`() {
            underTest.onSideSelected(SideSelection.RANDOM)

            val state = underTest.uiState.value as BlindModeUiState.Setup
            assertEquals(SideSelection.RANDOM, state.selectedSide)
        }

        @Test
        fun `GIVEN setup with Black WHEN onPlayClicked THEN state becomes Playing with correct side`() =
            runTest {
                prepareEngineForBlackGame()
                underTest.onSideSelected(SideSelection.BLACK)
                underTest.onPlayClicked()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertEquals(Side.BLACK, state.data.player)
                assertEquals(ChessEngine.DEFAULT_ELO, engine.currentElo)
            }

        @Test
        fun `GIVEN setup with White WHEN onPlayClicked THEN no engine move requested`() =
            runTest {
                underTest.onPlayClicked()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isThinking)
            }

        @Test
        fun `GIVEN setup with Black WHEN onPlayClicked THEN engine move played first`() =
            runTest {
                prepareEngineForBlackGame()
                underTest.onSideSelected(SideSelection.BLACK)
                underTest.onPlayClicked()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isThinking)
                assertTrue(state.moveHistory.isNotEmpty())
            }
    }

    @Nested
    internal inner class PlayingInteractions {
        @Test
        fun `GIVEN playing WHEN onSquareClicked on own piece THEN updates selection`() =
            runTest {
                startGame()

                underTest.onSquareClicked(Locus.e2)

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertEquals(Locus.e2, state.data.boardData.selection)
                assertTrue(state.data.boardData.availableMoves.isNotEmpty())
            }

        @Test
        fun `GIVEN playing WHEN onSquareClicked on empty square THEN no selection`() =
            runTest {
                startGame()

                underTest.onSquareClicked(Locus.e4)

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertNull(state.data.boardData.selection)
                assertTrue(state.data.boardData.availableMoves.isEmpty())
            }

        @Test
        fun `GIVEN selected piece WHEN move succeeds and engine responds THEN state updated`() =
            runTest {
                startGame()

                underTest.onSquareClicked(Locus.e2)
                underTest.onSquareClicked(Locus.e4)
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isThinking)
                assertNull(state.data.boardData.selection)
                assertTrue(state.moveHistory.isNotEmpty())
            }

        @Test
        fun `GIVEN selected piece WHEN clicking invalid target THEN clears selection`() =
            runTest {
                startGame()

                underTest.onSquareClicked(Locus.e2)
                underTest.onSquareClicked(Locus.e5)

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertNull(state.data.boardData.selection)
            }

        @Test
        fun `GIVEN playing WHEN onSquareClicked on opponent piece THEN no legal moves`() =
            runTest {
                startGame()

                underTest.onSquareClicked(Locus.e7)

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertTrue(state.data.boardData.availableMoves.isEmpty())
            }

        @Test
        fun `GIVEN thinking state WHEN onSquareClicked THEN ignored`() = runTest {
            startGame()

            underTest.onSquareClicked(Locus.e2)
            underTest.onSquareClicked(Locus.e4)

            val thinkingState = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(thinkingState.isThinking)

            underTest.onSquareClicked(Locus.d2)
            val stateAfter = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(stateAfter.isThinking)
            assertNull(stateAfter.data.boardData.selection)
        }
    }

    @Nested
    internal inner class ResignAndGameOver {
        @Test
        fun `GIVEN playing WHEN onResign THEN state becomes GameOver with Loss`() = runTest {
            startGame()

            underTest.onResign()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.GameOver
            assertEquals(BlindModeUiState.GameResult.Loss, state.result)
            assertNotNull(fakeOnComplete.lastResult)
        }

        @Test
        fun `GIVEN setup WHEN onResign THEN ignored`() {
            underTest.onResign()

            assertTrue(underTest.uiState.value is BlindModeUiState.Setup)
        }

        @Test
        fun `GIVEN game over WHEN onPlayAgain THEN state returns to Setup`() = runTest {
            startGame()
            underTest.onResign()
            advanceUntilIdle()
            assertTrue(underTest.uiState.value is BlindModeUiState.GameOver)

            underTest.onPlayAgain()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Setup
            assertTrue(state.isTrainingMode)
            assertTrue(engine.isStopped)
        }

        @Test
        fun `GIVEN onComplete called WHEN resign THEN result contains correct data`() =
            runTest {
                startGame()
                timer.advanceTimeBy(5000L)

                underTest.onResign()
                advanceUntilIdle()

                val result = fakeOnComplete.lastResult
                assertNotNull(result)
                assertTrue(result!!.isTrainingMode)
                assertEquals(ChessEngine.DEFAULT_ELO, result.opponentElo)
            }
    }

    @Nested
    internal inner class RevealBehavior {
        @Test
        fun `GIVEN playing WHEN onReveal THEN state is Playing with isRevealing true`() =
            runTest {
                startGame()

                underTest.onReveal()
                runCurrent()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertTrue(state.isRevealing)
            }

        @Test
        fun `GIVEN setup WHEN onReveal THEN ignored`() {
            underTest.onReveal()

            assertTrue(underTest.uiState.value is BlindModeUiState.Setup)
        }

        @Test
        fun `GIVEN playing WHEN reveal completes THEN isRevealing false`() =
            runTest {
                startGame()

                underTest.onReveal()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isRevealing)
            }

        @Test
        fun `GIVEN training mode WHEN reveal completes THEN reveal still available`() =
            runTest {
                startGame()

                underTest.onReveal()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertTrue(state.isRevealAvailable)
            }

        @Test
        fun `GIVEN rated mode WHEN reveal completes THEN reveal no longer available`() =
            runTest {
                underTest.onTrainingModeToggled(false)
                startGame()

                underTest.onReveal()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isRevealAvailable)
            }
    }

    @Nested
    internal inner class StatePropagation {
        @Test
        fun `GIVEN rated mode with Black WHEN playing THEN state carries settings`() =
            runTest {
                prepareEngineForBlackGame()
                underTest.onTrainingModeToggled(false)
                underTest.onSideSelected(SideSelection.BLACK)
                underTest.onPlayClicked()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isTrainingMode)
                assertEquals(Side.BLACK, state.data.player)
            }

        @Test
        fun `GIVEN rated mode WHEN reveal THEN Playing state carries settings`() =
            runTest {
                underTest.onTrainingModeToggled(false)
                startGame()

                underTest.onReveal()
                runCurrent()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertTrue(state.isRevealing)
                assertFalse(state.isTrainingMode)
            }

        @Test
        fun `GIVEN rated mode WHEN resign THEN GameOver state carries settings`() =
            runTest {
                underTest.onTrainingModeToggled(false)
                startGame()

                underTest.onResign()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.GameOver
                assertFalse(state.isTrainingMode)
            }

        @Test
        fun `GIVEN game over WHEN play again THEN Setup preserves isTrainingMode`() =
            runTest {
                underTest.onTrainingModeToggled(false)
                startGame()

                underTest.onResign()
                advanceUntilIdle()

                underTest.onPlayAgain()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Setup
                assertFalse(state.isTrainingMode)
            }
    }

    @Nested
    internal inner class AbandonDialog {
        @Test
        fun `GIVEN playing WHEN onBackPressed THEN dialog shown and returns true`() = runTest {
            startGame()

            val handled = underTest.onBackPressed()

            assertTrue(handled)
            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(state.isAbandonDialogShown)
        }

        @Test
        fun `GIVEN setup WHEN onBackPressed THEN returns false`() {
            val handled = underTest.onBackPressed()

            assertFalse(handled)
        }

        @Test
        fun `GIVEN abandon dialog shown WHEN onAbandonDismissed THEN dialog hidden`() =
            runTest {
                startGame()
                underTest.onBackPressed()

                underTest.onAbandonDismissed()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isAbandonDialogShown)
            }

        @Test
        fun `GIVEN abandon dialog shown WHEN onAbandonConfirmed THEN game resigned`() =
            runTest {
                startGame()
                underTest.onBackPressed()

                underTest.onAbandonConfirmed()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.GameOver
                assertEquals(BlindModeUiState.GameResult.Loss, state.result)
            }
    }

    private fun startGame() {
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private suspend fun prepareEngineForBlackGame() {
        repeat(3) { engine.calculateBestMove("drain") }
        engine.enqueueMoves(EngineMove(from = Locus.e2, to = Locus.e4))
    }
}
