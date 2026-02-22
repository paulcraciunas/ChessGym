package com.paulcraciunas.domain.api.boardvis

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

/**
 * Game engine for the "Move the Piece" board visualization game.
 *
 * This engine handles all the game logic including:
 * - Tracking game state (board, player position, visited squares)
 * - Validating moves
 * - Checking for captures
 * - Managing level progression
 */
interface MoveThePieceGameEngine {
    /**
     * Starts a new game with the given configuration.
     *
     * @param piece The piece type the player will control
     * @param requiredMoves Number of moves to complete the level
     * @param opposingPieceCount Number of opposing pieces on the board
     * @param isTrainingMode Whether the game is in training mode
     */
    fun startGame(
        piece: Piece,
        requiredMoves: Int,
        opposingPieceCount: Int,
        isTrainingMode: Boolean,
    )

    fun getState(): GameEngineState

    fun makeMove(to: Locus): MoveResult

    /**
     * Resets the game to initial state.
     */
    fun reset()
}

/**
 * Represents the current state of the game engine.
 */
data class GameEngineState(
    val playerPiece: Piece,
    val playerPieceLocus: Locus,
    val board: IBoard,
    val opposingPieces: Map<Locus, Piece>,
    val visitedSquares: Set<Locus>,
    val movesRemaining: Int,
    val currentScore: Int,
    val level: Int,
    val isTrainingMode: Boolean,
    val isGameOver: Boolean = false,
    val wasCaptured: Boolean = false
)

sealed class MoveResult {
    data object Invalid : MoveResult()
    data object Captured : MoveResult() // Player moved to a square under attack - game over
    data class Success(val newState: GameEngineState) : MoveResult()
    data class LevelComplete(val newState: GameEngineState) : MoveResult() // Level completed, advanced to next level
}
