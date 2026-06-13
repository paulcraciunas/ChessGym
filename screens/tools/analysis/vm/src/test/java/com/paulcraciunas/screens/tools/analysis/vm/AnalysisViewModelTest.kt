package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.domain.api.puzzles.PuzzleAnalysisData
import com.paulcraciunas.domain.impl.analysis.AnalyzePositionImpl
import com.paulcraciunas.game.engine.api.AnalysisResult
import com.paulcraciunas.game.engine.api.EngineLine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.engine.api.FakeChessEngine
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
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
internal class AnalysisViewModelTest {
    private val gameFactory = RealGameFactory()
    private val fenSerializer = FenSerializer(gameFactory)
    private val fakeEngine = FakeChessEngine()
    private val fakeAnalyzePosition = AnalyzePositionImpl(fakeEngine)
    private val appSettingsRepository = FakeAppSettingsRepository()
    private val fakeGetPuzzleFen = FakeGetPuzzleFen()

    private lateinit var underTest: AnalysisViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = AnalysisViewModel(
            dispatcher = testDispatcher,
            appScope = testScope,
            analyzePosition = fakeAnalyzePosition,
            sounds = SoundCoordinator(),
            fenSerializer = fenSerializer,
            getPuzzleFen = fakeGetPuzzleFen,
            appSettingsRepository = appSettingsRepository,
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
            assertNull(state.engineData)
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
        fun `GIVEN starting FEN WHEN loadPosition THEN board is loaded`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertEquals(Side.WHITE, state.data.player)
        }

        @Test
        fun `GIVEN puzzle FEN WHEN loadPosition THEN board shows position after firstMove`() = analysisTest {
            fakeGetPuzzleFen.setPuzzle(1, PuzzleAnalysisData(fen = STARTING_FEN, firstMove = "e2e4"))
            underTest.loadPosition(puzzleId = 1)
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertEquals(Side.BLACK, state.data.player)
        }

        @Test
        fun `GIVEN position loaded WHEN analysis result emitted THEN state updated`() = analysisTest {
            fakeEngine.enqueueAnalysisResults(
                listOf(
                    AnalysisResult(
                        depth = 10,
                        evaluation = Evaluation.Centipawns(30),
                        lines = listOf(
                            EngineLine(
                                1,
                                Evaluation.Centipawns(30),
                                listOf(
                                    EngineMove(from = Locus.e2, to = Locus.e4),
                                    EngineMove(from = Locus.e7, to = Locus.e5),
                                ),
                            ),
                        ),
                    )
                )
            )

            underTest.loadPosition()
            advanceUntilIdle()

            val data = underTest.uiState.value.engineData
            assertEquals(0.515f, data?.evaluation?.normalised)
            assertEquals(1, data?.engineLines?.size)
            assertEquals(10, data?.analysisDepth)
        }

        @Test
        fun `GIVEN analysis with top move WHEN result emitted THEN arrow is set`() = analysisTest {
            fakeEngine.enqueueAnalysisResults(
                listOf(
                    AnalysisResult(
                        depth = 5,
                        evaluation = Evaluation.Centipawns(18),
                        lines = listOf(
                            EngineLine(
                                1,
                                Evaluation.Centipawns(18),
                                listOf(EngineMove(from = Locus.e2, to = Locus.e4)),
                            ),
                        ),
                    )
                )
            )

            underTest.loadPosition()
            advanceUntilIdle()

            val topMove = underTest.uiState.value.engineData?.topMove
            assertNotNull(topMove)
            assertEquals(Locus.e2, topMove!!.from)
            assertEquals(Locus.e4, topMove.to)
        }

        @Test
        fun `GIVEN position loaded WHEN loadPosition THEN navigation resets`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            val state = underTest.uiState.value
            state.assertCannotNavigate()
        }
    }

    @Nested
    internal inner class FirstMove {
        @Test
        fun `GIVEN puzzle with firstMove WHEN loadPosition THEN position reflects firstMove played`() = analysisTest {
            fakeGetPuzzleFen.setPuzzle(1, PuzzleAnalysisData(fen = STARTING_FEN, firstMove = "e2e4"))
            underTest.loadPosition(puzzleId = 1)
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertEquals(Side.BLACK, state.data.player)
        }
    }

    @Nested
    internal inner class EvaluationNormalization {
        @Test
        fun `GIVEN white to move WHEN analysis returns positive score THEN displayed as positive`() = analysisTest {
            fakeEngine.enqueueAnalysisResults(
                listOf(
                    AnalysisResult(
                        depth = 10,
                        evaluation = Evaluation.Centipawns(150),
                        lines = listOf(
                            EngineLine(
                                1, Evaluation.Centipawns(150), listOf(
                                    EngineMove(from = Locus.e2, to = Locus.e4),
                                )
                            ),
                        ),
                    )
                )
            )

            underTest.loadPosition()
            advanceUntilIdle()

            assertEquals(0.575f, underTest.uiState.value.engineData?.evaluation?.normalised)
        }

        @Test
        fun `GIVEN black to move WHEN analysis returns negative score THEN negated to white perspective`() = analysisTest {
            fakeGetPuzzleFen.setPuzzle(1, PuzzleAnalysisData(fen = STARTING_FEN, firstMove = "e2e4"))
            fakeEngine.enqueueAnalysisResults(
                listOf(
                    AnalysisResult(
                        depth = 10,
                        evaluation = Evaluation.Centipawns(-350),
                        lines = listOf(
                            EngineLine(
                                1, Evaluation.Centipawns(-350), listOf(
                                    EngineMove(from = Locus.e7, to = Locus.e5),
                                )
                            ),
                        ),
                    )
                )
            )

            underTest.loadPosition(puzzleId = 1)
            advanceUntilIdle()

            assertEquals(0.675f, underTest.uiState.value.engineData?.evaluation?.normalised)
            assertEquals(0.675f, underTest.uiState.value.engineData?.engineLines[0]?.evaluation?.normalised)
        }

        @Test
        fun `GIVEN black to move WHEN analysis returns positive score THEN negated to show black winning`() = analysisTest {
            fakeGetPuzzleFen.setPuzzle(1, PuzzleAnalysisData(fen = STARTING_FEN, firstMove = "e2e4"))
            fakeEngine.enqueueAnalysisResults(
                listOf(
                    AnalysisResult(
                        depth = 10,
                        evaluation = Evaluation.Centipawns(200),
                        lines = listOf(
                            EngineLine(
                                1, Evaluation.Centipawns(200), listOf(
                                    EngineMove(from = Locus.e7, to = Locus.e5),
                                )
                            ),
                        ),
                    )
                )
            )

            underTest.loadPosition(puzzleId = 1)
            advanceUntilIdle()

            assertEquals(0.40f, underTest.uiState.value.engineData?.evaluation?.normalised)
        }

        @Test
        fun `GIVEN black to move WHEN mate score THEN negated`() = analysisTest {
            fakeGetPuzzleFen.setPuzzle(1, PuzzleAnalysisData(fen = STARTING_FEN, firstMove = "e2e4"))
            fakeEngine.enqueueAnalysisResults(
                listOf(
                    AnalysisResult(
                        depth = 20,
                        evaluation = Evaluation.Mate(3),
                        lines = listOf(
                            EngineLine(
                                1, Evaluation.Mate(3), listOf(
                                    EngineMove(from = Locus.e7, to = Locus.e5),
                                )
                            ),
                        ),
                    )
                )
            )

            underTest.loadPosition(puzzleId = 1)
            advanceUntilIdle()

            assertEquals(0.0f, underTest.uiState.value.engineData?.evaluation?.normalised)
            assertEquals("M-3", underTest.uiState.value.engineData?.evaluation?.display)
        }
    }

    @Nested
    internal inner class BoardInteraction {
        @Test
        fun `GIVEN position loaded WHEN square with piece clicked THEN board data updates`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            underTest.onSquareClicked(Locus.e2)
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertTrue(state.data.boardData.at(Locus.e2).piece?.isSelected == true)
        }

        @Test
        fun `GIVEN piece selected WHEN legal move made THEN move recorded in history`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()

            val state = underTest.uiState.value
            state.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN position loaded WHEN move made THEN analysis restarts with new position`() = analysisTest {
            fakeEngine.enqueueAnalysisResults(
                listOf(
                    AnalysisResult(
                        depth = 10,
                        evaluation = Evaluation.Centipawns(30),
                        lines = listOf(
                            EngineLine(
                                1,
                                Evaluation.Centipawns(30),
                                listOf(EngineMove(from = Locus.e2, to = Locus.e4)),
                            ),
                        ),
                    )
                )
            )
            fakeEngine.enqueueAnalysisResults(
                listOf(
                    AnalysisResult(
                        depth = 12,
                        evaluation = Evaluation.Centipawns(-15),
                        lines = listOf(
                            EngineLine(
                                1,
                                Evaluation.Centipawns(-15),
                                listOf(EngineMove(from = Locus.e7, to = Locus.e5)),
                            ),
                        ),
                    )
                )
            )

            underTest.loadPosition()
            advanceUntilIdle()
            assertEquals(0.515f, underTest.uiState.value.engineData?.evaluation?.normalised)

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()

            val data = underTest.uiState.value.engineData
            // After e4, it's Black's turn. Engine reports -15 from Black's perspective.
            // Normalized to White's perspective: +15
            assertEquals(0.5075f, data?.evaluation?.normalised)
            assertEquals(12, data?.analysisDepth)
        }

        @Test
        fun `GIVEN no position loaded WHEN square clicked THEN nothing crashes`() {
            underTest.onSquareClicked(Locus.e2)
            // Should not throw
        }
    }

    @Nested
    internal inner class Promotion {
        @Test
        fun `GIVEN pawn on 7th rank WHEN promotion move clicked THEN pendingPromotion has correct from square`() = analysisTest {
            appSettingsRepository.updateAutoPromote(false)
            advanceUntilIdle()

            loadPromotionPosition()
            advanceUntilIdle()

            playMove(Locus.e7, Locus.e8)
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertNotNull(state.data.promotion)
            assertEquals(Locus.e8, state.data.promotion?.at)
        }

        @Test
        fun `GIVEN pending promotion WHEN onPromote THEN move is recorded with correct from and piece`() = analysisTest {
            appSettingsRepository.updateAutoPromote(false)
            advanceUntilIdle()

            loadPromotionPosition()
            advanceUntilIdle()

            playMove(Locus.e7, Locus.e8)
            advanceUntilIdle()
            assertNotNull(underTest.uiState.value.data.promotion)

            underTest.onPromote(Piece.Queen)
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertNull(state.data.promotion)
            state.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN pending promotion WHEN onPromote THEN navigating back and replaying succeeds`() = analysisTest {
            appSettingsRepository.updateAutoPromote(false)
            advanceUntilIdle()

            loadPromotionPosition()
            advanceUntilIdle()

            playMove(Locus.e7, Locus.e8)
            advanceUntilIdle()
            underTest.onPromote(Piece.Rook)
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onJumpToStart()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()

            underTest.onJumpToEnd()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN autoPromote enabled WHEN promotion move clicked THEN move is auto-promoted and recorded`() = analysisTest {
            appSettingsRepository.updateAutoPromote(true)
            advanceUntilIdle()

            loadPromotionPosition()
            advanceUntilIdle()

            playMove(Locus.e7, Locus.e8)
            advanceUntilIdle()

            val state = underTest.uiState.value
            assertNull(state.data.promotion)
            underTest.uiState.value.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN autoPromote enabled WHEN promotion move played THEN navigating back and replaying succeeds`() = analysisTest {
            appSettingsRepository.updateAutoPromote(true)
            advanceUntilIdle()

            loadPromotionPosition()
            advanceUntilIdle()

            playMove(Locus.e7, Locus.e8)
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onJumpToStart()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()

            underTest.onJumpToEnd()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }

        private fun loadPromotionPosition() {
            fakeGetPuzzleFen.setPuzzle(99, PuzzleAnalysisData(fen = PROMOTION_FEN, firstMove = "h8h7"))
            underTest.loadPosition(puzzleId = 99)
        }
    }

    @Nested
    internal inner class Navigation {
        @Test
        fun `GIVEN moves played WHEN previousMove THEN shows previous position`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onPreviousMove()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()
        }

        @Test
        fun `GIVEN at start WHEN nextMove THEN shows next position`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()

            underTest.onPreviousMove()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()

            underTest.onNextMove()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN multiple moves WHEN jumpToStart THEN index is zero`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()
            playMove(Locus.e7, Locus.e5)
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onJumpToStart()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()
        }

        @Test
        fun `GIVEN navigated back WHEN jumpToEnd THEN index is at last move`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()
            playMove(Locus.e7, Locus.e5)
            advanceUntilIdle()

            underTest.onJumpToStart()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateForward()

            underTest.onJumpToEnd()
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN navigated back WHEN new move played THEN future is truncated`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()
            playMove(Locus.e7, Locus.e5)
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()

            underTest.onJumpToStart()
            advanceUntilIdle()

            playMove(Locus.d2, Locus.d4)
            advanceUntilIdle()
            underTest.uiState.value.assertCanNavigateBack()
        }

        @Test
        fun `GIVEN moves played WHEN navigating back and forth THEN playerSide stays consistent`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()
            val originalSide = underTest.uiState.value.data.player

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()
            playMove(Locus.e7, Locus.e5)
            advanceUntilIdle()

            underTest.onPreviousMove()
            advanceUntilIdle()
            assertEquals(originalSide, underTest.uiState.value.data.player)

            underTest.onNextMove()
            advanceUntilIdle()
            assertEquals(originalSide, underTest.uiState.value.data.player)

            underTest.onJumpToStart()
            advanceUntilIdle()
            assertEquals(originalSide, underTest.uiState.value.data.player)

            underTest.onJumpToEnd()
            advanceUntilIdle()
            assertEquals(originalSide, underTest.uiState.value.data.player)
        }

        @Test
        fun `GIVEN move played WHEN navigating to start THEN no last move highlights`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()

            underTest.onJumpToStart()
            advanceUntilIdle()

            val boardData = underTest.uiState.value.data.boardData
            val e2 = boardData.at(Locus.e2)
            val e4 = boardData.at(Locus.e4)
            assertFalse(e2.lastMove)
            assertFalse(e4.lastMove)
        }

        @Test
        fun `GIVEN move played WHEN navigating forward THEN last move highlights match replayed move`() = analysisTest {
            underTest.loadPosition()
            advanceUntilIdle()

            playMove(Locus.e2, Locus.e4)
            advanceUntilIdle()

            underTest.onJumpToStart()
            advanceUntilIdle()

            underTest.onNextMove()
            advanceUntilIdle()

            val boardData = underTest.uiState.value.data.boardData
            val e2 = boardData.at(Locus.e2)
            val e4 = boardData.at(Locus.e4)
            assertTrue(e2.lastMove)
            assertTrue(e4.lastMove)
        }
    }

    private fun analysisTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        backgroundScope.launch(testDispatcher) { underTest.uiState.collect {} }
        block()
    }

    private fun TestScope.playMove(from: Locus, to: Locus) {
        underTest.onSquareClicked(from)
        advanceUntilIdle()
        underTest.onSquareClicked(to)
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
        const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        // Black to move; after firstMove "h8h7", white can promote the e7 pawn
        const val PROMOTION_FEN = "7k/4P3/8/8/8/8/8/K7 b - - 0 1"
    }
}

private class FakeGetPuzzleFen : GetPuzzleFen {
    private val puzzles = mutableMapOf<Int, PuzzleAnalysisData>()

    fun setPuzzle(id: Int, data: PuzzleAnalysisData) {
        puzzles[id] = data
    }

    override suspend fun invoke(puzzleId: Int): PuzzleAnalysisData? = puzzles[puzzleId]
}
