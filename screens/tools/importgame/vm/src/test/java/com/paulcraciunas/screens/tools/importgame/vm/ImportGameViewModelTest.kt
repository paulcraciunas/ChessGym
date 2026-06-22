package com.paulcraciunas.screens.tools.importgame.vm

import com.paulcraciunas.domain.api.analysis.FakeAnalyzeFullGame
import com.paulcraciunas.domain.api.analysis.GameAnalysis
import com.paulcraciunas.domain.api.analysis.GameAnalysisProgress
import com.paulcraciunas.domain.api.analysis.MoveAnalysis
import com.paulcraciunas.domain.api.analysis.MoveClassification
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.PgnSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ImportGameViewModelTest {
    private val gameFactory = RealGameFactory()
    private val pgnSerializer = PgnSerializer(gameFactory)
    private val fakeAnalyzeFullGame = FakeAnalyzeFullGame()

    private lateinit var underTest: ImportGameViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = ImportGameViewModel(
            defaultDispatcher = testDispatcher,
            pgnSerializer = pgnSerializer,
            analysisUseCase = fakeAnalyzeFullGame,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    internal inner class Initialization {
        @Test
        fun `WHEN viewModel initialized THEN state is Setup`() = importTest {
            val state = underTest.uiState.value
            assertTrue(state is ImportGameUiState.Setup)
            assertFalse((state as ImportGameUiState.Setup).hasImportError)
        }
    }

    @Nested
    internal inner class Import {
        @Test
        fun `GIVEN valid PGN WHEN imported and analysis completes THEN state is Complete`() = importTest {
            setupAnalysisResult()
            underTest.onImport("1.e4 e5 2.Nf3 Nc6")
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertTrue(state is ImportGameUiState.Complete)
        }

        @Test
        fun `GIVEN invalid PGN WHEN imported THEN state is Setup with error`() = importTest {
            underTest.onImport("not a valid pgn")
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertTrue(state is ImportGameUiState.Setup)
            assertTrue((state as ImportGameUiState.Setup).hasImportError)
        }

        @Test
        fun `GIVEN valid PGN WHEN imported THEN player info extracted from metadata`() = importTest {
            setupAnalysisResult()
            underTest.onImport("[White \"Magnus\"]\n[Black \"Hikaru\"]\n1.e4 e5 2.Nf3 Nc6")
            advanceUntilIdle()

            val state = underTest.uiState.value as ImportGameUiState.Complete
            assertEquals("Magnus", state.playerInfo.whiteName)
            assertEquals("Hikaru", state.playerInfo.blackName)
        }

        @Test
        fun `GIVEN analysis in progress WHEN abandon THEN state returns to Setup`() = importTest {
            underTest.onImport("1.e4 e5 2.Nf3 Nc6")

            underTest.onAbandonConfirmed()

            val state = underTest.uiState.value
            assertTrue(state is ImportGameUiState.Setup)
        }
    }

    @Nested
    internal inner class Navigation {
        @Test
        fun `GIVEN Complete state WHEN onJumpToStart THEN at first position`() = importTest {
            importAndComplete()
            underTest.onJumpToEnd()

            underTest.onJumpToStart()

            val state = underTest.uiState.value as ImportGameUiState.Complete
            assertEquals(0, state.currentMoveIndex)
            assertFalse(state.canNavigateBack)
            assertTrue(state.canNavigateForward)
        }

        @Test
        fun `GIVEN Complete state at start WHEN onNextMove THEN navigates forward`() = importTest {
            importAndComplete()

            underTest.onNextMove()

            val state = underTest.uiState.value as ImportGameUiState.Complete
            assertEquals(1, state.currentMoveIndex)
            assertTrue(state.canNavigateBack)
            assertTrue(state.canNavigateForward)
        }

        @Test
        fun `GIVEN Complete state at end WHEN onPreviousMove THEN navigates back`() = importTest {
            importAndComplete()
            underTest.onJumpToEnd()

            underTest.onPreviousMove()

            val state = underTest.uiState.value as ImportGameUiState.Complete
            assertTrue(state.canNavigateForward)
        }

        @Test
        fun `GIVEN Complete state WHEN onMoveSelected THEN navigates to that move`() = importTest {
            importAndComplete()

            underTest.onMoveSelected(3)

            val state = underTest.uiState.value as ImportGameUiState.Complete
            assertEquals(3, state.currentMoveIndex)
        }

        @Test
        fun `GIVEN Complete state at start WHEN onPreviousMove THEN stays at start`() = importTest {
            importAndComplete()

            underTest.onPreviousMove()

            val state = underTest.uiState.value as ImportGameUiState.Complete
            assertEquals(0, state.currentMoveIndex)
            assertFalse(state.canNavigateBack)
        }
    }

    @Nested
    internal inner class FlipBoard {
        @Test
        fun `GIVEN Complete state WHEN onFlipBoard THEN orientation changes`() = importTest {
            importAndComplete()

            val orientationBefore = (underTest.uiState.value as ImportGameUiState.Complete).orientation
            underTest.onFlipBoard()
            val orientationAfter = (underTest.uiState.value as ImportGameUiState.Complete).orientation

            assertFalse(orientationBefore == orientationAfter)
        }
    }

    @Nested
    internal inner class BlunderOverlay {
        @Test
        fun `GIVEN no blunder WHEN move selected THEN no overlay`() = importTest {
            importAndComplete()

            underTest.onMoveSelected(1)

            val state = underTest.uiState.value as ImportGameUiState.Complete
            assertNull(state.blunderOverlay)
        }
    }

    private fun importTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        backgroundScope.launch(testDispatcher) { underTest.uiState.collect {} }
        block()
    }

    private fun TestScope.importAndComplete() {
        setupAnalysisResult()
        underTest.onImport("1.e4 e5 2.Nf3 Nc6")
        advanceUntilIdle()
    }

    private fun setupAnalysisResult() {
        val moves = listOf(
            moveAnalysis(1, "e4", MoveClassification.Good),
            moveAnalysis(2, "e5", MoveClassification.Good),
            moveAnalysis(3, "Nf3", MoveClassification.Great),
            moveAnalysis(4, "Nc6", MoveClassification.Good),
        )
        fakeAnalyzeFullGame.setResult(
            GameAnalysisProgress.Analyzing(1, 4, moves[0]),
            GameAnalysisProgress.Analyzing(2, 4, moves[1]),
            GameAnalysisProgress.Analyzing(3, 4, moves[2]),
            GameAnalysisProgress.Analyzing(4, 4, moves[3]),
            GameAnalysisProgress.Completed(
                GameAnalysis(
                    moves = moves,
                    averageCentipawnLoss = 5f,
                    blunders = emptyList(),
                    mistakes = emptyList(),
                    inaccuracies = emptyList(),
                    brilliancies = emptyList(),
                )
            ),
        )
    }

    private fun moveAnalysis(index: Int, algebraic: String, classification: MoveClassification): MoveAnalysis =
        MoveAnalysis(
            moveIndex = index,
            moveAlgebraic = algebraic,
            evaluation = Evaluation.Centipawns(10),
            centipawnLoss = 5,
            classification = classification,
            bestMove = null,
        )
}
