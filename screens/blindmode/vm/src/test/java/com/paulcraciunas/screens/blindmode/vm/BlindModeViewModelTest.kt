package com.paulcraciunas.screens.blindmode.vm

import com.paulcraciunas.domain.api.blindmode.FakeOnBlindModeGameComplete
import com.paulcraciunas.domain.api.general.FakeTimer
import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.domain.impl.engine.EngineOrchestratorImpl
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.FakeChessEngine
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.FakeUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
            dispatcher = testDispatcher,
            timer = timer,
            randomFactory = FixedRandomFactory(),
            onComplete = fakeOnComplete,
            engineOrchestrator = EngineOrchestratorImpl(
                chessEngine = engine,
                serializer = FenSerializer(RealGameFactory()),
                dispatcher = testDispatcher,
            ),
            appSettingsRepository = appSettings,
            userRepository = userRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN initial state WHEN created THEN uiState is Setup with defaults`() = blindModeTest {
        val state = underTest.uiState.value as BlindModeUiState.Setup
        assertTrue(state.isTrainingMode)
        assertEquals(SideSelection.WHITE, state.selectedSide)
    }

    @Nested
    internal inner class SetupInteractions {
        @Test
        fun `GIVEN setup WHEN onTrainingModeToggled false THEN isTrainingMode is false`() = blindModeTest {
            underTest.onTrainingModeToggled(false)
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Setup
            assertFalse(state.isTrainingMode)
        }

        @Test
        fun `GIVEN setup WHEN onSideSelected Black THEN selectedSide updates`() = blindModeTest {
            underTest.onSideSelected(SideSelection.BLACK)
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Setup
            assertEquals(SideSelection.BLACK, state.selectedSide)
        }

        @Test
        fun `GIVEN setup WHEN onSideSelected Random THEN selectedSide is random`() = blindModeTest {
            underTest.onSideSelected(SideSelection.RANDOM)
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Setup
            assertEquals(SideSelection.RANDOM, state.selectedSide)
        }

        @Test
        fun `GIVEN setup with Black WHEN onPlayClicked THEN state becomes Playing with correct side`() = blindModeTest {
            prepareEngineForBlackGame()
            underTest.onSideSelected(SideSelection.BLACK)
            underTest.onPlayClicked()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertEquals(Side.BLACK, state.data.player)
            assertEquals(ChessEngine.DEFAULT_ELO, engine.currentElo)
        }

        @Test
        fun `GIVEN setup with White WHEN onPlayClicked THEN board is interactive`() = blindModeTest {
            underTest.onPlayClicked()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertFalse(state.isThinking)
            assertNull(state.data.outcome)
        }

        @Test
        fun `GIVEN setup with Black WHEN onPlayClicked THEN engine move played first`() = blindModeTest {
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
        fun `GIVEN playing WHEN onSquareClicked on own piece THEN updates selection`() = blindModeTest {
            startGame()

            underTest.onSquareClicked(Locus.e2)
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertEquals(Locus.e2, state.data.boardData.selection)
            assertTrue(state.data.boardData.availableMoves.isNotEmpty())
        }

        @Test
        fun `GIVEN playing WHEN onSquareClicked on empty square THEN no selection`() = blindModeTest {
            startGame()

            underTest.onSquareClicked(Locus.e4)
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertNull(state.data.boardData.selection)
            assertTrue(state.data.boardData.availableMoves.isEmpty())
        }

        @Test
        fun `GIVEN selected piece WHEN move succeeds and engine responds THEN state updated`() = blindModeTest {
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
        fun `GIVEN selected piece WHEN clicking invalid target THEN clears selection`() = blindModeTest {
            startGame()

            underTest.onSquareClicked(Locus.e2)
            underTest.onSquareClicked(Locus.e5)
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertNull(state.data.boardData.selection)
        }

        @Test
        fun `GIVEN playing WHEN onSquareClicked on opponent piece THEN no legal moves`() = blindModeTest {
            startGame()

            underTest.onSquareClicked(Locus.e7)
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(state.data.boardData.availableMoves.isEmpty())
        }

        @Test
        fun `GIVEN engine thinking WHEN onSquareClicked THEN ignored`() = blindModeTest {
            startGame()

            underTest.onSquareClicked(Locus.e2)
            advanceUntilIdle()
            underTest.onSquareClicked(Locus.e4)

            underTest.onSquareClicked(Locus.d2)

            advanceUntilIdle()
            val stateAfter = underTest.uiState.value as BlindModeUiState.Playing
            assertFalse(stateAfter.isThinking)
            assertNull(stateAfter.data.boardData.selection)
        }
    }

    @Nested
    internal inner class ResignAndGameOver {
        @Test
        fun `GIVEN playing WHEN onResign confirmed THEN state becomes GameOver with Loss`() = blindModeTest {
            startGame()

            underTest.onResign()
            underTest.onAbandonConfirmed()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.GameOver
            assertEquals(Outcome.Lost, state.data.outcome)
            assertNotNull(fakeOnComplete.lastResult)
        }

        @Test
        fun `GIVEN setup WHEN onResign THEN ignored`() = blindModeTest {
            underTest.onResign()
            advanceUntilIdle()

            assertTrue(underTest.uiState.value is BlindModeUiState.Setup)
        }

        @Test
        fun `GIVEN game over WHEN onPlayAgain THEN state returns to Setup`() = blindModeTest {
            startGame()
            underTest.onResign()
            underTest.onAbandonConfirmed()
            advanceUntilIdle()
            assertTrue(underTest.uiState.value is BlindModeUiState.GameOver)

            underTest.onPlayAgain()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Setup
            assertTrue(state.isTrainingMode)
            assertTrue(engine.isStopped)
        }

        @Test
        fun `GIVEN onComplete called WHEN resign confirmed THEN result contains correct data`() = blindModeTest {
            startGame()
            timer.advanceTimeBy(5000L)

            underTest.onResign()
            underTest.onAbandonConfirmed()
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
        fun `GIVEN playing WHEN onReveal THEN state is Playing with isRevealing true`() = blindModeTest {
            startGame()

            underTest.onReveal()
            runCurrent()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(state.isRevealing)
        }

        @Test
        fun `GIVEN setup WHEN onReveal THEN ignored`() = blindModeTest {
            underTest.onReveal()
            advanceUntilIdle()

            assertTrue(underTest.uiState.value is BlindModeUiState.Setup)
        }

        @Test
        fun `GIVEN playing WHEN reveal completes THEN isRevealing false`() = blindModeTest {
            startGame()

            underTest.onReveal()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertFalse(state.isRevealing)
        }

        @Test
        fun `GIVEN training mode WHEN reveal completes THEN reveal still available`() = blindModeTest {
            startGame()

            underTest.onReveal()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(state.isRevealAvailable)
        }

        @Test
        fun `GIVEN rated mode WHEN reveal completes THEN reveal no longer available`() = blindModeTest {
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
        fun `GIVEN rated mode with Black WHEN playing THEN state carries settings`() = blindModeTest {
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
        fun `GIVEN rated mode WHEN reveal THEN Playing state carries settings`() = blindModeTest {
            underTest.onTrainingModeToggled(false)
            startGame()

            underTest.onReveal()
            runCurrent()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(state.isRevealing)
            assertFalse(state.isTrainingMode)
        }

        @Test
        fun `GIVEN rated mode WHEN resign confirmed THEN GameOver state carries settings`() = blindModeTest {
            underTest.onTrainingModeToggled(false)
            startGame()

            underTest.onResign()
            underTest.onAbandonConfirmed()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.GameOver
            assertFalse(state.isTrainingMode)
        }

        @Test
        fun `GIVEN game over WHEN play again THEN Setup preserves isTrainingMode`() = blindModeTest {
            underTest.onTrainingModeToggled(false)
            startGame()

            underTest.onResign()
            underTest.onAbandonConfirmed()
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
        fun `GIVEN playing WHEN onBackPressed THEN dialog shown and returns true`() = blindModeTest {
            startGame()

            val handled = underTest.onBackPressed()
            advanceUntilIdle()

            assertTrue(handled)
            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertTrue(state.isAbandonDialogShown)
        }

        @Test
        fun `GIVEN setup WHEN onBackPressed THEN returns false`() = blindModeTest {
            val handled = underTest.onBackPressed()

            assertFalse(handled)
        }

        @Test
        fun `GIVEN abandon dialog shown WHEN onAbandonDismissed THEN dialog hidden`() = blindModeTest {
            startGame()
            underTest.onBackPressed()
            advanceUntilIdle()

            underTest.onAbandonDismissed()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.Playing
            assertFalse(state.isAbandonDialogShown)
        }

        @Test
        fun `GIVEN abandon dialog shown WHEN onAbandonConfirmed THEN game resigned`() = blindModeTest {
            startGame()
            underTest.onBackPressed()

            underTest.onAbandonConfirmed()
            advanceUntilIdle()

            val state = underTest.uiState.value as BlindModeUiState.GameOver
            assertEquals(Outcome.Lost, state.data.outcome)
        }
    }

    private fun blindModeTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        backgroundScope.launch(testDispatcher) { underTest.uiState.collect {} }
        block()
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
