package com.paulcraciunas.game.engine.impl

import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.impl.uci.UciCommand
import com.paulcraciunas.game.engine.impl.uci.UciResponse
import com.paulcraciunas.utils.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * [ChessEngine] implementation backed by Stockfish 11 via JNI.
 * Communicates with the native engine using the UCI protocol over stdin/stdout pipes.
 */
// TODO Paul: This class needs to be thoroughly tested!!
internal class UciChessEngine @Inject constructor(
    @DefaultDispatcher val dispatcher: CoroutineDispatcher,
    val uci: UciFacade,
) : ChessEngine {

    override suspend fun initialize() {
        uci.startEngine()
        withContext(dispatcher) {
            uci.execute<UciResponse.Done>(UciCommand.Init)
            uci.execute<UciResponse.Ready>(UciCommand.IsReady)
        }
    }

    override suspend fun startNewGame(elo: Int) {
        withContext(dispatcher) {
            uci.execute<UciResponse.Done>(UciCommand.NewGame)
            uci.execute<UciResponse.Done>(UciCommand.LimitElo)
            uci.execute<UciResponse.Done>(UciCommand.SetElo(elo))
            uci.execute<UciResponse.Ready>(UciCommand.IsReady)
        }
    }

    override suspend fun calculateBestMove(fen: String): EngineMove =
        withContext(dispatcher) {
            uci.execute<UciResponse.Done>(UciCommand.SetPosition(fen))
            val bestMove = uci.execute<UciResponse.BestMove>(UciCommand.SetMoveTime())
            bestMove.engineMove
        }

    override suspend fun stop() {
        withContext(dispatcher) {
            uci.execute<UciResponse.Done>(UciCommand.Stop)
        }
    }

    override suspend fun shutdown() {
        withContext(dispatcher) {
            uci.execute<UciResponse.Done>(UciCommand.Quit)
        }
    }
}
