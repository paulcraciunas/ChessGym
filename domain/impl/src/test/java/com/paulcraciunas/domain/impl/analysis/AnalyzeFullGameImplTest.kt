package com.paulcraciunas.domain.impl.analysis

import com.paulcraciunas.domain.api.analysis.GameAnalysisProgress
import com.paulcraciunas.domain.api.analysis.MoveClassification
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.engine.api.FakeChessEngine
import com.paulcraciunas.game.engine.api.PositionEvaluation
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.serializer.api.Serializer
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AnalyzeFullGameImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeEngine = FakeChessEngine()
    private val noOpSerializer = object : Serializer {
        override fun from(gameString: String): Game = error("Not needed")
        override fun of(game: Game): String = error("Not needed")
        override fun of(puzzle: Puzzle): String = error("Not needed")
    }

    private val underTest = AnalyzeFullGameImpl(
        engine = fakeEngine,
        serializer = noOpSerializer,
        dispatcher = testDispatcher,
    )

    @Test
    fun `GIVEN fewer than 2 positions WHEN analyze THEN throws`() = runTest(testDispatcher) {
        assertThrows<IllegalArgumentException> {
            underTest.analyze(positions = listOf("fen1" to null), depth = 15).toList()
        }
    }

    @Test
    fun `GIVEN 3 positions WHEN analyze THEN emits 2 progress updates and 1 completed`() = runTest(testDispatcher) {
        val positions = listOf(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" to null,
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1" to "e4",
            "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2" to "e5",
        )

        val results = underTest.analyze(positions, depth = 15).toList()

        assertEquals(3, results.size)
        assertTrue(results[0] is GameAnalysisProgress.Analyzing)
        assertTrue(results[1] is GameAnalysisProgress.Analyzing)
        assertTrue(results[2] is GameAnalysisProgress.Completed)
    }

    @Test
    fun `GIVEN positions WHEN analyze THEN progress reports correct move indices`() = runTest(testDispatcher) {
        val positions = listOf(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" to null,
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1" to "e4",
            "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2" to "e5",
        )

        val results = underTest.analyze(positions, depth = 15).toList()

        val progress1 = results[0] as GameAnalysisProgress.Analyzing
        assertEquals(1, progress1.currentMove)
        assertEquals(2, progress1.totalMoves)

        val progress2 = results[1] as GameAnalysisProgress.Analyzing
        assertEquals(2, progress2.currentMove)
        assertEquals(2, progress2.totalMoves)
    }

    @Test
    fun `GIVEN equal evaluations WHEN analyze THEN centipawn loss is computed correctly`() = runTest(testDispatcher) {
        val positions = listOf("start" to null, "after_move_1" to null)
        // Starting position: mover sees +30
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(30), bestMove = null)
        )
        // After move: opponent sees +30 (meaning mover lost nothing, from mover's view: -30)
        // CPL = 30 + 30 = 60?? No — if opponent sees +30, that means mover's position worsened.
        // Actually: if starting eval is +30 (mover sees advantage) and after their move opponent
        // also sees +30 (meaning from mover's new perspective it's -30), CPL = 30 + 30 = 60.
        // To get CPL = 0, opponent must see -30 (mover maintained their +30 advantage).
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(-30), bestMove = null)
        )

        val results = underTest.analyze(positions, depth = 18).toList()
        val completed = results.last() as GameAnalysisProgress.Completed

        assertEquals(0, completed.analysis.moves[0].centipawnLoss)
        assertEquals(MoveClassification.Great, completed.analysis.moves[0].classification)
    }

    @Test
    fun `GIVEN white blunder WHEN analyze THEN detects blunder correctly`() = runTest(testDispatcher) {
        val positions = listOf("start" to null, "after_white_blunder" to null)
        // Before move: mover (White) sees +100
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(100), bestMove = null)
        )
        // After blunder: opponent (Black) sees +150 (meaning White went from +100 to -150)
        // CPL = 100 + 150 = 250
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(150), bestMove = null)
        )

        val results = underTest.analyze(positions, depth = 18).toList()
        val completed = results.last() as GameAnalysisProgress.Completed

        assertEquals(250, completed.analysis.moves[0].centipawnLoss)
        assertEquals(MoveClassification.Blunder, completed.analysis.moves[0].classification)
        assertEquals(listOf(1), completed.analysis.blunders)
    }

    @Test
    fun `GIVEN black blunder WHEN analyze THEN detects blunder correctly`() = runTest(testDispatcher) {
        val positions = listOf("start" to null, "after_white_move" to null, "after_black_blunder" to null)
        // Position 0 (White to move): White sees 0
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(0), bestMove = null)
        )
        // Position 1 (Black to move after White's move): Black sees -20 (meaning White gained +20)
        // White's CPL = 0 + (-20) = -20, coerced to 0. White didn't lose anything.
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(-20), bestMove = null)
        )
        // Position 2 (White to move after Black's blunder): White sees +200
        // Black's CPL = (-20) + 200 = 180
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(200), bestMove = null)
        )

        val results = underTest.analyze(positions, depth = 18).toList()
        val completed = results.last() as GameAnalysisProgress.Completed

        val whiteMove = completed.analysis.moves[0]
        assertEquals(0, whiteMove.centipawnLoss)

        val blackMove = completed.analysis.moves[1]
        assertEquals(180, blackMove.centipawnLoss)
        assertEquals(MoveClassification.Blunder, blackMove.classification)
        assertEquals(listOf(2), completed.analysis.blunders)
    }

    @Test
    fun `GIVEN completed analysis WHEN checking game analysis THEN computes average CPL`() = runTest(testDispatcher) {
        val positions = listOf("start" to null, "move1" to null, "move2" to null, "move3" to null)
        // Position 0 (White to move): +30
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(30), bestMove = null)
        )
        // Position 1 (Black to move): -10 → White's CPL = 30 + (-10) = 20
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(-10), bestMove = null)
        )
        // Position 2 (White to move): +15 → Black's CPL = (-10) + 15 = 5
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(15), bestMove = null)
        )
        // Position 3 (Black to move): -5 → White's CPL = 15 + (-5) = 10
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(-5), bestMove = null)
        )

        val results = underTest.analyze(positions, depth = 18).toList()
        val completed = results.last() as GameAnalysisProgress.Completed

        val moves = completed.analysis.moves
        assertEquals(20, moves[0].centipawnLoss)
        assertEquals(5, moves[1].centipawnLoss)
        assertEquals(10, moves[2].centipawnLoss)

        val expectedAvg = (20 + 5 + 10).toFloat() / 3
        assertEquals(expectedAvg, completed.analysis.averageCentipawnLoss)
    }

    @Test
    fun `GIVEN all positions evaluated WHEN analyze THEN engine receives all FENs`() = runTest(testDispatcher) {
        val positions = listOf("fen_start" to null, "fen_1" to null, "fen_2" to null)

        underTest.analyze(positions, depth = 15).toList()

        assertEquals(3, fakeEngine.evaluatedFens.size)
        assertEquals("fen_start", fakeEngine.evaluatedFens[0])
        assertEquals("fen_1", fakeEngine.evaluatedFens[1])
        assertEquals("fen_2", fakeEngine.evaluatedFens[2])
    }

    @Test
    fun `GIVEN mate evaluation WHEN analyze THEN handles large centipawn equivalent`() = runTest(testDispatcher) {
        val positions = listOf("start" to null, "after_mate_blunder" to null)
        // White sees 0 before their move
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(depth = 18, evaluation = Evaluation.Centipawns(0), bestMove = null)
        )
        // After White's blunder: Black sees Mate in 3 (positive = good for side to move = Black)
        // CPL = 0 + 10000 = 10000
        fakeEngine.enqueuePositionEvaluation(
            PositionEvaluation(
                depth = 18,
                evaluation = Evaluation.Mate(3),
                bestMove = com.paulcraciunas.game.engine.api.EngineMove(Locus.e2, Locus.e4),
            )
        )

        val results = underTest.analyze(positions, depth = 18).toList()
        val completed = results.last() as GameAnalysisProgress.Completed

        assertEquals(MoveClassification.Blunder, completed.analysis.moves[0].classification)
        assertTrue(completed.analysis.moves[0].centipawnLoss > 100)
    }
}
