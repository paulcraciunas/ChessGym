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
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
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
    private val appSettingsRepository = FakeAppSettingsRepository()

    private lateinit var underTest: AnalysisViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = AnalysisViewModel(
            fenSerializer = fenSerializer,
            analyzePosition = fakeAnalyzePosition,
            appSettingsRepository = appSettingsRepository,
            gameInteractor = gameFactory.gameInteractor(),
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

        @Test
        fun `WHEN viewModel created THEN navigation state is at zero`() {
            val state = underTest.uiState.value
            state.assertCannotNavigate()
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
            assertEquals("e2".loc(), arrow!!.from)
            assertEquals("e4".loc(), arrow.to)
        }

        @Test
        fun `GIVEN position loaded WHEN loadPosition THEN navigation resets`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            val state = underTest.uiState.value
            state.assertCannotNavigate()
        }
    }

    @Nested
    internal inner class FirstMove {
        @Test
        fun `GIVEN firstMove provided WHEN loadPosition THEN move is played`() = runTest {
            underTest.loadPosition(firstMove = "e2e4")
            advanceUntilIdle()

            val state = underTest.uiState.value
            state.assertCanNavigateBack()
            // After e4, it's black's turn
            assertEquals(Side.BLACK, state.playerSide)
        }
    }

    @Nested
    internal inner class EvaluationNormalization {
        @Test
        fun `GIVEN white to move WHEN analysis returns positive score THEN displayed as positive`() = runTest {
            fakeEngine.enqueueAnalysisResults(listOf(
                AnalysisResult(
                    depth = 10,
                    evaluation = Evaluation.Centipawns(150),
                    lines = listOf(
                        EngineLine(1, Evaluation.Centipawns(150), listOf(
                            EngineMove(from = "e2".loc(), to = "e4".loc()),
                        )),
                    ),
                )
            ))

            underTest.loadPosition()
            advanceUntilIdle()

            assertEquals(Evaluation.Centipawns(150), underTest.uiState.value.evaluation)
        }

        @Test
        fun `GIVEN black to move WHEN analysis returns negative score THEN negated to white perspective`() = runTest {
            val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
            fakeEngine.enqueueAnalysisResults(listOf(
                AnalysisResult(
                    depth = 10,
                    evaluation = Evaluation.Centipawns(-350),
                    lines = listOf(
                        EngineLine(1, Evaluation.Centipawns(-350), listOf(
                            EngineMove(from = "e7".loc(), to = "e5".loc()),
                        )),
                    ),
                )
            ))

            underTest.loadPosition(fen)
            advanceUntilIdle()

            assertEquals(Evaluation.Centipawns(350), underTest.uiState.value.evaluation)
            assertEquals(Evaluation.Centipawns(350), underTest.uiState.value.engineLines[0].evaluation)
        }

        @Test
        fun `GIVEN black to move WHEN analysis returns positive score THEN negated to show black winning`() = runTest {
            val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
            fakeEngine.enqueueAnalysisResults(listOf(
                AnalysisResult(
                    depth = 10,
                    evaluation = Evaluation.Centipawns(200),
                    lines = listOf(
                        EngineLine(1, Evaluation.Centipawns(200), listOf(
                            EngineMove(from = "e7".loc(), to = "e5".loc()),
                        )),
                    ),
                )
            ))

            underTest.loadPosition(fen)
            advanceUntilIdle()

            assertEquals(Evaluation.Centipawns(-200), underTest.uiState.value.evaluation)
        }

        @Test
        fun `GIVEN black to move WHEN mate score THEN negated`() = runTest {
            val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
            fakeEngine.enqueueAnalysisResults(listOf(
                AnalysisResult(
                    depth = 20,
                    evaluation = Evaluation.Mate(3),
                    lines = listOf(
                        EngineLine(1, Evaluation.Mate(3), listOf(
                            EngineMove(from = "e7".loc(), to = "e5".loc()),
                        )),
                    ),
                )
            ))

            underTest.loadPosition(fen)
            advanceUntilIdle()

            assertEquals(Evaluation.Mate(-3), underTest.uiState.value.evaluation)
        }
    }

    @Nested
    internal inner class BoardInteraction {
        @Test
        fun `GIVEN position loaded WHEN square with piece clicked THEN board data updates`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            underTest.onSquareClicked("e2".loc())

            val state = underTest.uiState.value
            assertNotNull(state.boardData)
        }

        @Test
        fun `GIVEN piece selected WHEN legal move made THEN move recorded in history`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove("e2", "e4")
            advanceUntilIdle()

            val state = underTest.uiState.value
            state.assertCanNavigateBack()
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

            playMove("e2", "e4")
            advanceUntilIdle()

            val state = underTest.uiState.value
            // After e4, it's Black's turn. Engine reports -15 from Black's perspective.
            // Normalized to White's perspective: +15
            assertEquals(Evaluation.Centipawns(15), state.evaluation)
            assertEquals(12, state.analysisDepth)
        }

        @Test
        fun `GIVEN no position loaded WHEN square clicked THEN nothing crashes`() {
            underTest.onSquareClicked(Locus(File.e, Rank.`2`))
            // Should not throw
        }
    }

    @Nested
    internal inner class Promotion {
        @Test
        fun `GIVEN pawn on 7th rank WHEN promotion move clicked THEN pendingPromotion has correct from square`() = runTest {
            val fen = PROMOTION_FEN
            appSettingsRepository.updateAutoPromote(false)
            advanceUntilIdle()

            underTest.loadPosition(fen)
            advanceUntilIdle()

            playMove("e7", "e8")

            val state = underTest.uiState.value
            val pending = state.pendingPromotion
            assertNotNull(pending)
            assertEquals(Locus(File.e, Rank.`7`), pending!!.from)
            assertEquals(Locus(File.e, Rank.`8`), pending.to)
        }

        @Test
        fun `GIVEN pending promotion WHEN onPromote THEN move is recorded with correct from and piece`() = runTest {
            val fen = PROMOTION_FEN
            appSettingsRepository.updateAutoPromote(false)
            advanceUntilIdle()

            underTest.loadPosition(fen)
            advanceUntilIdle()

            playMove("e7", "e8")
            assertNotNull(underTest.uiState.value.pendingPromotion)

            underTest.onPromote(Piece.Queen)
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertNull(state.pendingPromotion)
            state.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN pending promotion WHEN onPromote THEN navigating back and replaying succeeds`() = runTest {
            appSettingsRepository.updateAutoPromote(false)
            advanceUntilIdle()

            underTest.loadPosition(PROMOTION_FEN)
            advanceUntilIdle()

            playMove("e7", "e8")
            underTest.onPromote(Piece.Rook)
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onJumpToStart()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()

            // Navigate forward -- this would crash if from was incorrectly recorded
            underTest.onNextMove()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN autoPromote enabled WHEN promotion move clicked THEN move is auto-promoted and recorded`() = runTest {
            val fen = PROMOTION_FEN
            appSettingsRepository.updateAutoPromote(true)
            advanceUntilIdle()

            underTest.loadPosition(fen)
            advanceUntilIdle()

            playMove("e7", "e8")
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertNull(state.pendingPromotion)
            underTest.uiState.value.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN autoPromote enabled WHEN promotion move played THEN navigating back and replaying succeeds`() = runTest {
            val fen = PROMOTION_FEN
            appSettingsRepository.updateAutoPromote(true)
            advanceUntilIdle()

            underTest.loadPosition(fen)
            advanceUntilIdle()

            playMove("e7", "e8")
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onJumpToStart()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()

            // Navigate forward -- validates the recorded move can be replayed correctly
            underTest.onNextMove()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }
    }

    @Nested
    internal inner class Navigation {
        @Test
        fun `GIVEN moves played WHEN previousMove THEN shows previous position`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove("e2", "e4")
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onPreviousMove()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()
        }

        @Test
        fun `GIVEN at start WHEN nextMove THEN shows next position`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove("e2", "e4")
            advanceUntilIdle()

            underTest.onPreviousMove()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()

            underTest.onNextMove()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN multiple moves WHEN jumpToStart THEN index is zero`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove("e2", "e4")
            advanceUntilIdle()
            playMove("e7", "e5")
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onJumpToStart()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()
        }

        @Test
        fun `GIVEN navigated back WHEN jumpToEnd THEN index is at last move`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove("e2", "e4")
            advanceUntilIdle()
            playMove("e7", "e5")
            advanceUntilIdle()

            underTest.onJumpToStart()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()

            underTest.onJumpToEnd()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN navigated back WHEN new move played THEN future is truncated`() = runTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove("e2", "e4")
            advanceUntilIdle()
            playMove("e7", "e5")
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onJumpToStart()
            advanceUntilIdle()

            playMove("d2", "d4")
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }
    }

    private fun playMove(from: String, to: String) {
        underTest.onSquareClicked(from.loc())
        underTest.onSquareClicked(to.loc())
    }

    private fun AnalysisUiState.assertCannotNavigate() {
        assertEquals(false, canNavigateBack)
        assertEquals(false, canNavigateForward)
    }

    private fun AnalysisUiState.assertCanNavigateBack() {
        assertEquals(true, canNavigateBack)
        assertEquals(false, canNavigateForward)
    }

    private fun AnalysisUiState.assertCanNavigateForward() {
        assertEquals(false, canNavigateBack)
        assertEquals(true, canNavigateForward)
    }

    private companion object {
        // White pawn on e7, e8 empty, black king on h8, white king on a1
        const val PROMOTION_FEN = "7k/4P3/8/8/8/8/8/K7 w - - 0 1"
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
