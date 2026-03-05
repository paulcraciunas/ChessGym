package com.paulcraciunas.screens.blindmode.vm

import com.paulcraciunas.domain.api.blindmode.FakeBlindModeOrchestrator
import com.paulcraciunas.domain.api.blindmode.FakeOnBlindModeGameComplete
import com.paulcraciunas.domain.api.general.FakeTimer
import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.screens.common.controls.SideSelection
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
    private val fakeOrchestrator = FakeBlindModeOrchestrator()
    private val fakeOnComplete = FakeOnBlindModeGameComplete()
    private val timer = FakeTimer()
    private val fakeRandomFactory = FixedRandomFactory()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var underTest: BlindModeViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = BlindModeViewModel(
            orchestrator = fakeOrchestrator,
            onComplete = fakeOnComplete,
            timer = timer,
            randomFactory = fakeRandomFactory,
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
    inner class SetupInteractions {
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
        fun `GIVEN setup WHEN onPlayClicked THEN state becomes Playing with correct side`() =
            runTest {
                fakeOrchestrator.enqueueEngineMove(
                    EngineMove(from = "e2".loc(), to = "e4".loc())
                )
                underTest.onSideSelected(SideSelection.BLACK)
                underTest.onPlayClicked()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertEquals(Side.BLACK, state.playerSide)
                assertEquals(ChessEngine.DEFAULT_ELO, fakeOrchestrator.startedWithElo)
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
        fun `GIVEN setup with Black WHEN onPlayClicked THEN engine move requested first`() =
            runTest {
                fakeOrchestrator.enqueueEngineMove(
                    EngineMove(from = "e2".loc(), to = "e4".loc())
                )
                underTest.onSideSelected(SideSelection.BLACK)
                underTest.onPlayClicked()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isThinking)
                assertTrue(state.moveHistory.isNotEmpty())
            }
    }

    @Nested
    inner class PlayingInteractions {
        @Test
        fun `GIVEN playing WHEN onSquareClicked on own piece THEN updates selection`() =
            runTest {
                startGame()

                underTest.onSquareClicked("e2".loc())

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertEquals("e2".loc(), state.selectedSquare)
                assertTrue(state.legalMoves.isNotEmpty())
            }

        @Test
        fun `GIVEN playing WHEN onSquareClicked on empty square THEN clears selection`() =
            runTest {
                startGame()

                underTest.onSquareClicked("e4".loc())

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertNull(state.selectedSquare)
                assertTrue(state.legalMoves.isEmpty())
            }

        @Test
        fun `GIVEN selected piece WHEN move succeeds and engine responds THEN state updated`() =
            runTest {
                startGame()
                fakeOrchestrator.enqueueEngineMove(
                    EngineMove(from = "e7".loc(), to = "e5".loc())
                )

                underTest.onSquareClicked("e2".loc())
                underTest.onSquareClicked("e4".loc())
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isThinking)
                assertNull(state.selectedSquare)
                assertTrue(state.moveHistory.isNotEmpty())
            }

        @Test
        fun `GIVEN selected piece WHEN clicking invalid target THEN re-evaluates as selection`() =
            runTest {
                startGame()

                underTest.onSquareClicked("e2".loc())
                underTest.onSquareClicked("e5".loc())

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertNull(state.selectedSquare)
            }

        @Test
        fun `GIVEN playing WHEN onSquareClicked on opponent piece THEN clears selection`() =
            runTest {
                startGame()

                underTest.onSquareClicked("e7".loc())

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertNull(state.selectedSquare)
            }

        @Test
        fun `GIVEN thinking state WHEN onSquareClicked THEN ignored`() = runTest {
            startGame()
            fakeOrchestrator.enqueueEngineMove(
                EngineMove(from = "e7".loc(), to = "e5".loc())
            )

            underTest.onSquareClicked("e2".loc())
            underTest.onSquareClicked("e4".loc())

            val thinkingState = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(thinkingState.isThinking)

            underTest.onSquareClicked("d2".loc())
            val stateAfter = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(stateAfter.isThinking)
            assertNull(stateAfter.selectedSquare)
        }
    }

    @Nested
    inner class ResignAndGameOver {
        @Test
        fun `GIVEN playing WHEN onResign THEN state becomes GameOver with Loss`() = runTest {
            startGame()

            underTest.onResign()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.GameOver
            assertEquals(BlindModeUiState.GameResult.Loss, state.result)
            assertTrue(fakeOrchestrator.wasResigned)
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
            assertEquals(SideSelection.WHITE, state.selectedSide)
            assertTrue(fakeOrchestrator.wasReset)
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
    inner class RevealBehavior {
        @Test
        fun `GIVEN playing WHEN onReveal THEN state transitions to Revealing`() = runTest {
            startGame()

            underTest.onReveal()
            runCurrent()

            assertTrue(underTest.uiState.value is BlindModeUiState.Revealing)
        }

        @Test
        fun `GIVEN setup WHEN onReveal THEN ignored`() {
            underTest.onReveal()

            assertTrue(underTest.uiState.value is BlindModeUiState.Setup)
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
    inner class StatePropagation {
        @Test
        fun `GIVEN rated mode with Black WHEN playing THEN state carries settings`() =
            runTest {
                fakeOrchestrator.enqueueEngineMove(
                    EngineMove(from = "e2".loc(), to = "e4".loc())
                )
                underTest.onTrainingModeToggled(false)
                underTest.onSideSelected(SideSelection.BLACK)
                underTest.onPlayClicked()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Playing
                assertFalse(state.isTrainingMode)
                assertEquals(SideSelection.BLACK, state.selectedSide)
            }

        @Test
        fun `GIVEN rated mode WHEN reveal THEN Revealing state carries settings`() =
            runTest {
                underTest.onTrainingModeToggled(false)
                underTest.onSideSelected(SideSelection.BLACK)
                fakeOrchestrator.enqueueEngineMove(
                    EngineMove(from = "e2".loc(), to = "e4".loc())
                )
                startGame()

                underTest.onReveal()
                runCurrent()

                val state = underTest.uiState.value as BlindModeUiState.Revealing
                assertFalse(state.isTrainingMode)
                assertEquals(SideSelection.BLACK, state.selectedSide)
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
        fun `GIVEN game over WHEN play again THEN Setup restores previous settings`() =
            runTest {
                underTest.onTrainingModeToggled(false)
                underTest.onSideSelected(SideSelection.BLACK)
                fakeOrchestrator.enqueueEngineMove(
                    EngineMove(from = "e2".loc(), to = "e4".loc())
                )
                startGame()

                underTest.onResign()
                advanceUntilIdle()

                underTest.onPlayAgain()
                advanceUntilIdle()

                val state = underTest.uiState.value as BlindModeUiState.Setup
                assertFalse(state.isTrainingMode)
                assertEquals(SideSelection.BLACK, state.selectedSide)
            }
    }

    @Nested
    inner class AbandonDialog {
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
                assertTrue(fakeOrchestrator.wasResigned)
            }
    }

    private fun startGame() {
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
