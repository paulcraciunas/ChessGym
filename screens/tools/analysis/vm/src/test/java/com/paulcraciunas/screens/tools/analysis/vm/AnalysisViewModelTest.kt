package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.domain.api.analysis.AnalyzePosition
import com.paulcraciunas.game.engine.api.AnalysisResult
import com.paulcraciunas.game.engine.api.EngineLine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.engine.api.FakeChessEngine
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.FenSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AnalysisViewModelTest {

    private val gameFactory = RealGameFactory()
    private val fenSerializer = FenSerializer(gameFactory)
    private val fakeEngine = FakeChessEngine()
    private val fakeAnalyzePosition = FakeAnalyzePosition(fakeEngine)

    private lateinit var underTest: AnalysisViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = AnalysisViewModel(
            fenSerializer = fenSerializer,
            analyzePosition = fakeAnalyzePosition,
        ).also { it.disableThrottling() }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    internal inner class Initialization {
        @Test
        fun `WHEN viewModel created THEN initial state has no analysis`() {
            val state = underTest.uiState.value
            assertNull(state.evaluation)
            assertTrue(state.engineLines.isEmpty())
            assertNull(state.topMoveArrow)
        }
    }

    @Nested
    internal inner class PositionLoading {
        @Test
        fun `GIVEN starting FEN WHEN loadPosition THEN board is loaded`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertEquals(Side.WHITE, state.playerSide)
        }

        @Test
        fun `GIVEN custom FEN WHEN loadPosition THEN board shows custom position`() = runTest {
            val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
            underTest.loadPosition(fen)
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertEquals(Side.BLACK, state.playerSide)
        }

        @Test
        fun `GIVEN position loaded WHEN analysis result emitted THEN state updated`() = runTest {
            fakeEngine.enqueueAnalysisResults(listOf(
                AnalysisResult(
                    depth = 10,
                    evaluation = Evaluation.Centipawns(30),
                    lines = listOf(
                        EngineLine(
                            1,
                            Evaluation.Centipawns(30),
                            listOf(
                                EngineMove(from = "e2".loc(), to = "e4".loc()),
                                EngineMove(from = "e7".loc(), to = "e5".loc()),
                            ),
                        ),
                    ),
                )
            ))

            underTest.loadPosition()
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertEquals(Evaluation.Centipawns(30), state.evaluation)
            assertEquals(1, state.engineLines.size)
            assertEquals(10, state.analysisDepth)
        }

        @Test
        fun `GIVEN analysis with top move WHEN result emitted THEN arrow is set`() = runTest {
            fakeEngine.enqueueAnalysisResults(listOf(
                AnalysisResult(
                    depth = 5,
                    evaluation = Evaluation.Centipawns(18),
                    lines = listOf(
                        EngineLine(
                            1,
                            Evaluation.Centipawns(18),
                            listOf(EngineMove(from = "e2".loc(), to = "e4".loc())),
                        ),
                    ),
                )
            ))

            underTest.loadPosition()
            advanceUntilIdle()

            val arrow = underTest.uiState.value.topMoveArrow
            assertNotNull(arrow)
            assertEquals(Locus(File.e, Rank.`2`), arrow!!.from)
            assertEquals(Locus(File.e, Rank.`4`), arrow.to)
        }
    }

    @Nested
    internal inner class BoardInteraction {
        @Test
        fun `GIVEN position loaded WHEN square with piece clicked THEN square selected`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            underTest.onSquareClicked(Locus(File.e, Rank.`2`))

            val state = underTest.uiState.value
            assertEquals(Locus(File.e, Rank.`2`), state.selectedSquare)
            assertTrue(state.legalMoves.isNotEmpty())
        }

        @Test
        fun `GIVEN piece selected WHEN legal move made THEN move applied`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            underTest.onSquareClicked(Locus(File.e, Rank.`2`))
            underTest.onSquareClicked(Locus(File.e, Rank.`4`))
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertNull(state.selectedSquare)
            assertTrue(state.legalMoves.isEmpty())
        }

        @Test
        fun `GIVEN position loaded WHEN move made THEN analysis restarts with new position`() = runTest {
            fakeEngine.enqueueAnalysisResults(listOf(
                AnalysisResult(
                    depth = 10,
                    evaluation = Evaluation.Centipawns(30),
                    lines = listOf(
                        EngineLine(
                            1,
                            Evaluation.Centipawns(30),
                            listOf(EngineMove(from = "e2".loc(), to = "e4".loc())),
                        ),
                    ),
                )
            ))
            fakeEngine.enqueueAnalysisResults(listOf(
                AnalysisResult(
                    depth = 12,
                    evaluation = Evaluation.Centipawns(-15),
                    lines = listOf(
                        EngineLine(
                            1,
                            Evaluation.Centipawns(-15),
                            listOf(EngineMove(from = "e7".loc(), to = "e5".loc())),
                        ),
                    ),
                )
            ))

            underTest.loadPosition()
            advanceUntilIdle()
            assertEquals(Evaluation.Centipawns(30), underTest.uiState.value.evaluation)

            underTest.onSquareClicked(Locus(File.e, Rank.`2`))
            underTest.onSquareClicked(Locus(File.e, Rank.`4`))
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertEquals(Evaluation.Centipawns(-15), state.evaluation)
            assertEquals(12, state.analysisDepth)
        }

        @Test
        fun `GIVEN empty square clicked WHEN no selection THEN nothing happens`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            underTest.onSquareClicked(Locus(File.e, Rank.`4`))

            assertNull(underTest.uiState.value.selectedSquare)
        }

        @Test
        fun `GIVEN no position loaded WHEN square clicked THEN nothing happens`() {
            underTest.onSquareClicked(Locus(File.e, Rank.`2`))
            assertNull(underTest.uiState.value.selectedSquare)
        }
    }

    @Nested
    internal inner class FlipBoard {
        @Test
        fun `GIVEN white perspective WHEN flip board THEN switches to black`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()
            assertEquals(Side.WHITE, underTest.uiState.value.playerSide)

            underTest.onFlipBoard()

            assertEquals(Side.BLACK, underTest.uiState.value.playerSide)
        }

        @Test
        fun `GIVEN black perspective WHEN flip board again THEN switches back to white`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            underTest.onFlipBoard()
            underTest.onFlipBoard()

            assertEquals(Side.WHITE, underTest.uiState.value.playerSide)
        }
    }
}

private class FakeAnalyzePosition(
    private val engine: FakeChessEngine,
) : AnalyzePosition {
    override suspend fun prepare() {
        engine.prepareForAnalysis()
    }

    override fun invoke(fen: String): Flow<AnalysisResult> {
        return engine.analyzePosition(fen)
    }

    override suspend fun stopAnalysis() {
        engine.stopAnalysis()
    }

    override suspend fun shutdown() {
        engine.shutdown()
    }
}
