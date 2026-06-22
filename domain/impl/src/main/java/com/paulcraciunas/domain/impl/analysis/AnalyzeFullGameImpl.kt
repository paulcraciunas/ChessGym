package com.paulcraciunas.domain.impl.analysis

import com.paulcraciunas.domain.api.analysis.AnalyzeFullGame
import com.paulcraciunas.domain.api.analysis.GameAnalysis
import com.paulcraciunas.domain.api.analysis.GameAnalysisProgress
import com.paulcraciunas.domain.api.analysis.MoveAnalysis
import com.paulcraciunas.domain.api.analysis.MoveClassification
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.engine.api.PositionEvaluation
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.serializer.api.Serializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

class AnalyzeFullGameImpl(
    private val engine: ChessEngine,
    private val serializer: Serializer,
    private val dispatcher: CoroutineDispatcher,
) : AnalyzeFullGame {

    override fun analyze(game: Game, depth: Int): Flow<GameAnalysisProgress> {
        val positions = extractPositions(game)
        return analyze(positions, depth)
    }

    override fun analyze(positions: List<String>, depth: Int): Flow<GameAnalysisProgress> = flow {
        require(positions.size >= 2) { "A game must have at least 2 positions (start + 1 move)" }

        engine.prepareForAnalysis()

        val evaluations = mutableListOf<PositionEvaluation>()
        val moveAnalyses = mutableListOf<MoveAnalysis>()
        val totalMoves = positions.size - 1

        val startingEval = engine.evaluatePosition(positions.first(), depth)
        evaluations.add(startingEval)

        for (moveIndex in 1..totalMoves) {
            if (!currentCoroutineContext().isActive) return@flow

            val fen = positions[moveIndex]
            val positionEval = engine.evaluatePosition(fen, depth)
            evaluations.add(positionEval)

            val moveAnalysis = buildMoveAnalysis(
                moveIndex = moveIndex,
                fen = fen,
                previousEval = evaluations[moveIndex - 1],
                currentEval = positionEval,
            )
            moveAnalyses.add(moveAnalysis)

            emit(
                GameAnalysisProgress.Analyzing(
                    currentMove = moveIndex,
                    totalMoves = totalMoves,
                    latestMoveAnalysis = moveAnalysis,
                )
            )
        }

        emit(GameAnalysisProgress.Completed(analysis = buildGameAnalysis(moveAnalyses)))
    }.flowOn(dispatcher)

    private fun buildMoveAnalysis(
        moveIndex: Int,
        fen: String,
        previousEval: PositionEvaluation,
        currentEval: PositionEvaluation,
    ): MoveAnalysis {
        val centipawnLoss = computeCentipawnLoss(previousEval, currentEval)
        return MoveAnalysis(
            moveIndex = moveIndex,
            fen = fen,
            evaluation = currentEval.evaluation,
            centipawnLoss = centipawnLoss,
            classification = MoveClassifier.classify(centipawnLoss),
            bestMove = previousEval.bestMove,
        )
    }

    /**
     * Engine eval is always from the side-to-move's perspective.
     * previousEval: from the mover's perspective (it was their turn).
     * currentEval: from the opponent's perspective (it's now their turn).
     * Mover's eval after = -currentEval, so CPL = previousCp - (-currentCp) = previousCp + currentCp.
     */
    private fun computeCentipawnLoss(
        previousEval: PositionEvaluation,
        currentEval: PositionEvaluation,
    ): Int {
        val previousCp = toCentipawns(previousEval.evaluation)
        val currentCp = toCentipawns(currentEval.evaluation)
        return (previousCp + currentCp).coerceAtLeast(0)
    }

    private fun toCentipawns(evaluation: Evaluation): Int = when (evaluation) {
        is Evaluation.Centipawns -> evaluation.value
        is Evaluation.Mate -> if (evaluation.movesToMate > 0) MATE_SCORE else -MATE_SCORE
    }

    private fun buildGameAnalysis(moves: List<MoveAnalysis>): GameAnalysis {
        val totalCpLoss = moves.sumOf { it.centipawnLoss }
        val averageCpLoss = if (moves.isNotEmpty()) totalCpLoss.toFloat() / moves.size else 0f

        return GameAnalysis(
            moves = moves,
            averageCentipawnLoss = averageCpLoss,
            blunders = moves.filterByClassification(MoveClassification.Blunder),
            mistakes = moves.filterByClassification(MoveClassification.Mistake),
            inaccuracies = moves.filterByClassification(MoveClassification.Inaccuracy),
            brilliancies = moves.filterByClassification(MoveClassification.Brilliant),
        )
    }

    private fun List<MoveAnalysis>.filterByClassification(
        classification: MoveClassification,
    ): List<Int> = filter { it.classification == classification }.map { it.moveIndex }

    private fun extractPositions(game: Game): List<String> {
        val originalIndex = game.currentMoveIndex
        val positions = mutableListOf<String>()

        game.undoAll()
        positions.add(serializer.of(game))

        while (game.canReplay()) {
            game.replayNext()
            positions.add(serializer.of(game))
        }

        // Restore original position
        game.undoAll()
        repeat(originalIndex) { game.replayNext() }

        return positions
    }

    companion object {
        private const val MATE_SCORE = 10_000
    }
}
