package com.paulcraciunas.domain.api.blindmode

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side

/**
 * Orchestrates a blind mode game, coordinating between the player's [Game] and
 * the chess engine for the opponent's moves.
 */
interface BlindModeOrchestrator {
    suspend fun initialize()
    suspend fun startGame(elo: Int, side: Side = Side.WHITE): Game
    suspend fun requestEngineMove(): Ply
    suspend fun stop()
}
