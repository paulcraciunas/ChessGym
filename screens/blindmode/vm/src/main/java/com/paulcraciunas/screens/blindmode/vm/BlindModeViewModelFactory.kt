package com.paulcraciunas.screens.blindmode.vm

import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineOrchestrator
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.logic.builders.Builders
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.EngineOpponent
import com.paulcraciunas.screens.data.GameNavigation
import com.paulcraciunas.screens.data.GamePlayableBoard
import com.paulcraciunas.screens.data.engine.SingleSessionConfiguration
import com.paulcraciunas.user.api.UserRepository

internal fun blindModeConfiguration(): SingleSessionConfiguration = SingleSessionConfiguration(
    gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.Terminate,
    hints = SingleSessionConfiguration.HintMode.Unlimited,
)

internal class BlindModeSession(
    private val userRepository: UserRepository,
    private val engineOrchestrator: EngineOrchestrator,
) {
    private var player: Side = Side.WHITE

    fun withStartingSide(turn: Side) {
        player = turn
    }

    suspend fun createSession(): BoardSession {
        val elo = userRepository.get().ratings.blindMode.coerceAtLeast(ChessEngine.DEFAULT_ELO)
        val game = Builders.gameFactory().builder()
            .withDefaultBoard()
            .withRating(elo)
            .buildGame()
        return BoardSession(
            navigation = GameNavigation(game),
            opponent = EngineOpponent(orchestrator = engineOrchestrator, game = game),
        ).load(GamePlayableBoard(game = game, player = player))
    }
}
