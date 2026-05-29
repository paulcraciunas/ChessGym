package com.paulcraciunas.domain.impl.blindmode

import com.paulcraciunas.domain.api.blindmode.BlindModeOrchestrator
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.logic.builders.Builders
import com.paulcraciunas.serializer.api.Serializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class BlindModeOrchestratorImpl(
    private val chessEngine: ChessEngine,
    private val serializer: Serializer,
    private val dispatcher: CoroutineDispatcher,
) : BlindModeOrchestrator {
    private val gameFactory: GameFactory = Builders.gameFactory()
    private var playerSide: Side = Side.WHITE
    private lateinit var game: Game

    override suspend fun initialize() = withContext(dispatcher) {
        chessEngine.initialize()
    }

    override suspend fun startGame(elo: Int, side: Side): Game = withContext(dispatcher) {
        game = gameFactory.builder()
            .withDefaultBoard()
            .withRating(elo)
            .buildGame()
        game.start()
        playerSide = side
        chessEngine.startNewGame(elo)
        return@withContext game
    }

    override suspend fun requestEngineMove(): Ply = withContext(dispatcher) {
        val fen = serializer.of(game)
        val engineMove = chessEngine.calculateBestMove(fen)

        val ply = game.plies(engineMove.from)
            .first { it.to == engineMove.to }

        engineMove.promotion?.let { ply.promote(it) }

        return@withContext ply
    }

    override suspend fun stop() = withContext(dispatcher) {
        chessEngine.stop()
    }
}
