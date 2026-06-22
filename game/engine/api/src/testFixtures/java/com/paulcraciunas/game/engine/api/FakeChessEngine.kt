package com.paulcraciunas.game.engine.api

import com.paulcraciunas.game.logic.api.board.Locus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.LinkedList

/**
 * A fake [ChessEngine] that returns pre-programmed moves.
 * By default, preloaded with the Italian Game opening for Black:
 *   1. ... e5  2. ... Nc6  3. ... Bc5
 *
 * Additional moves can be enqueued via [enqueueMoves].
 */
class FakeChessEngine : ChessEngine {
    private val moveQueue: LinkedList<EngineMove> = LinkedList()
    private val analysisResults: LinkedList<List<AnalysisResult>> = LinkedList()
    private val positionEvaluations: LinkedList<PositionEvaluation> = LinkedList()
    var isInitialized: Boolean = false
        private set
    var isPreparedForAnalysis: Boolean = false
        private set
    var isAnalysisStopped: Boolean = false
        private set
    var isStopped: Boolean = false
        private set
    var isShutdown: Boolean = false
        private set
    var currentElo: Int = ChessEngine.DEFAULT_ELO
        private set
    var lastReceivedFen: String? = null
        private set
    var evaluatedFens: MutableList<String> = mutableListOf()
        private set

    init {
        loadItalianGameDefense()
    }

    fun enqueueMoves(vararg moves: EngineMove) {
        moveQueue.addAll(moves)
    }

    fun enqueueAnalysisResults(results: List<AnalysisResult>) {
        analysisResults.add(results)
    }

    fun enqueuePositionEvaluation(evaluation: PositionEvaluation) {
        positionEvaluations.add(evaluation)
    }

    override suspend fun initialize() {
        isInitialized = true
    }

    override suspend fun startNewGame(elo: Int) {
        currentElo = elo
    }

    override suspend fun calculateBestMove(fen: String): EngineMove {
        lastReceivedFen = fen
        return moveQueue.poll() ?: throw IllegalStateException("FakeChessEngine: no moves enqueued")
    }

    override suspend fun prepareForAnalysis() {
        isInitialized = true
        isPreparedForAnalysis = true
    }

    override fun analyzePosition(fen: String, multiPvCount: Int): Flow<AnalysisResult> = flow {
        lastReceivedFen = fen
        val results = analysisResults.poll() ?: listOf(defaultAnalysisResult())
        results.forEach { emit(it) }
    }

    override suspend fun evaluatePosition(fen: String, depth: Int): PositionEvaluation {
        lastReceivedFen = fen
        evaluatedFens.add(fen)
        return positionEvaluations.poll() ?: defaultPositionEvaluation(depth)
    }

    override suspend fun stopAnalysis() {
        isAnalysisStopped = true
    }

    override suspend fun stop() {
        isStopped = true
    }

    override suspend fun shutdown() {
        isShutdown = true
    }

    private fun loadItalianGameDefense() {
        enqueueMoves(
            EngineMove(from = Locus.e7, to = Locus.e5),
            EngineMove(from = Locus.b8, to = Locus.c6),
            EngineMove(from = Locus.f8, to = Locus.c5),
        )
    }

    private fun defaultAnalysisResult(): AnalysisResult = AnalysisResult(
        depth = 15,
        evaluation = Evaluation.Centipawns(30),
        lines = listOf(
            EngineLine(
                rank = 1,
                evaluation = Evaluation.Centipawns(30),
                moves = listOf(
                    EngineMove(from = Locus.e2, to = Locus.e4),
                    EngineMove(from = Locus.e7, to = Locus.e5),
                ),
            ),
        ),
    )

    private fun defaultPositionEvaluation(depth: Int): PositionEvaluation = PositionEvaluation(
        depth = depth,
        evaluation = Evaluation.Centipawns(30),
        bestMove = EngineMove(from = Locus.e2, to = Locus.e4),
    )
}
