package com.paulcraciunas.game.engine.impl

import com.paulcraciunas.game.engine.api.AnalysisResult
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.PositionEvaluation
import com.paulcraciunas.game.engine.impl.uci.AnalysisAccumulator
import com.paulcraciunas.game.engine.impl.uci.InfoLineParser
import com.paulcraciunas.game.engine.impl.uci.UciCommand
import com.paulcraciunas.game.engine.impl.uci.UciResponse
import com.paulcraciunas.utils.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val BEST_MOVE_PREFIX = "bestmove"

/**
 * [ChessEngine] implementation backed by Stockfish 11 via JNI.
 * Communicates with the native engine using the UCI protocol over stdin/stdout pipes.
 */
internal class UciChessEngine @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val uci: UciFacade,
) : ChessEngine {
    @Volatile
    private var isEngineRunning: Boolean = false
    private val analysisLock = Mutex()

    override suspend fun initialize(): Unit = withContext(dispatcher) {
        if (isEngineRunning) return@withContext
        uci.startEngine()
        uci.execute<UciResponse.Initialized>(UciCommand.Init)
        uci.execute<UciResponse.Ready>(UciCommand.IsReady)
        isEngineRunning = true
    }

    override suspend fun startNewGame(elo: Int): Unit = withContext(dispatcher) {
        initialize()
        uci.execute<UciResponse.Done>(UciCommand.NewGame)
        uci.execute<UciResponse.Done>(UciCommand.LimitElo)
        uci.execute<UciResponse.Done>(UciCommand.SetElo(elo))
        uci.execute<UciResponse.Ready>(UciCommand.IsReady)
    }

    override suspend fun calculateBestMove(fen: String): EngineMove = withContext(dispatcher) {
        uci.execute<UciResponse.Done>(UciCommand.SetPosition(fen))
        val bestMove = uci.execute<UciResponse.BestMove>(UciCommand.SetMoveTime())
        bestMove.engineMove
    }

    override suspend fun prepareForAnalysis(): Unit = withContext(dispatcher) {
        initialize()
        uci.execute<UciResponse.Done>(UciCommand.NewGame)
        uci.execute<UciResponse.Done>(UciCommand.DisableLimitStrength)
        uci.execute<UciResponse.Ready>(UciCommand.IsReady)
    }

    override fun analyzePosition(fen: String, multiPvCount: Int): Flow<AnalysisResult> = flow {
        uci.sendCommand(UciCommand.Stop)

        analysisLock.withLock {
            uci.execute<UciResponse.Ready>(UciCommand.IsReady)
            uci.execute<UciResponse.Done>(UciCommand.SetMultiPV(multiPvCount))
            uci.execute<UciResponse.Done>(UciCommand.SetPosition(fen))

            uci.sendCommand(UciCommand.GoInfinite)
            val accumulator = AnalysisAccumulator(multiPvCount)
            while (currentCoroutineContext().isActive) {
                val line = uci.readLine()
                if (line.startsWith(BEST_MOVE_PREFIX)) break

                val parsed = InfoLineParser.parse(line) ?: continue
                accumulator.process(parsed)?.let { emit(it) }
            }
        }
    }.conflate().flowOn(dispatcher)

    override suspend fun evaluatePosition(fen: String, depth: Int): PositionEvaluation = withContext(dispatcher) {
        uci.sendCommand(UciCommand.Stop)

        analysisLock.withLock {
            uci.execute<UciResponse.Ready>(UciCommand.IsReady)
            uci.execute<UciResponse.Done>(UciCommand.SetMultiPV(1))
            uci.execute<UciResponse.Done>(UciCommand.SetPosition(fen))

            val result = uci.execute<UciResponse.EvaluatedBestMove>(UciCommand.GoDepth(depth))
            PositionEvaluation(
                depth = result.depth,
                evaluation = result.evaluation,
                bestMove = result.engineMove,
            )
        }
    }

    override suspend fun stopAnalysis(): Unit = withContext(dispatcher) {
        uci.sendCommand(UciCommand.Stop)
    }

    override suspend fun stop(): Unit = withContext(dispatcher) {
        uci.execute<UciResponse.Done>(UciCommand.Stop)
    }

    override suspend fun shutdown(): Unit = withContext(dispatcher) {
        uci.execute<UciResponse.Done>(UciCommand.Quit)
        uci.shutdownEngine()
        isEngineRunning = false
    }
}
