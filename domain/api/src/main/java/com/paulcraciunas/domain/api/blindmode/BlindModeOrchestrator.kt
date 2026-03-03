package com.paulcraciunas.domain.api.blindmode

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus

/**
 * Orchestrates a blind mode game, coordinating between the player's [Game] and
 * the chess engine for the opponent's moves.
 */
interface BlindModeOrchestrator {
    suspend fun startGame(elo: Int, side: Side = Side.WHITE)
    fun selectSquare(locus: Locus): SelectionResult
    fun playMove(from: Locus, to: Locus): PlayResult
    suspend fun requestEngineMove(): EnginePlayResult
    fun resign()
    fun board(): IBoard
    fun currentFen(): String
    fun moveHistory(): List<Ply>
    fun pliesFrom(locus: Locus): List<Locus>
    fun playerSide(): Side
    suspend fun reset()
}

sealed class SelectionResult {
    data class PieceSelected(val locus: Locus, val legalMoves: List<Locus>) : SelectionResult()
    data object NoPiece : SelectionResult()
    data object WrongSide : SelectionResult()
}

sealed class PlayResult {
    data class Success(val ply: Ply) : PlayResult()
    data class GameOver(val ply: Ply, val result: Result) : PlayResult()
    data object Invalid : PlayResult()
}

sealed class EnginePlayResult {
    data class Success(val ply: Ply) : EnginePlayResult()
    data class GameOver(val ply: Ply, val result: Result) : EnginePlayResult()
}
