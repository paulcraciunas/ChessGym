package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.boardvis.GameEngineState
import com.paulcraciunas.domain.api.boardvis.GenerateMoveThePieceBoard
import com.paulcraciunas.domain.api.boardvis.MoveResult
import com.paulcraciunas.domain.api.boardvis.MoveThePieceGameEngine
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.MoveValidator
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import javax.inject.Inject

/**
 * Implementation of [MoveThePieceGameEngine] that manages the game state
 * and uses [MoveValidator] for move validation and attack checking.
 */
class MoveThePieceGameEngineImpl @Inject constructor(
    private val generateBoard: GenerateMoveThePieceBoard,
    gameFactory: GameFactory
) : MoveThePieceGameEngine {

    private val moveValidator: MoveValidator = gameFactory.moveValidator()
    private var state: GameEngineState? = null
    private var board: IBoard? = null

    override fun startGame(
        piece: Piece,
        requiredMoves: Int,
        opposingPieceCount: Int,
        isTrainingMode: Boolean,
    ) {
        val boardData = generateBoard(
            piece = piece,
            requiredMoves = requiredMoves,
            opposingPieceCount = opposingPieceCount
        )
        board = boardData.board

        state = GameEngineState(
            playerPiece = piece,
            playerPieceLocus = boardData.playerPieceLocus,
            board = boardData.board,
            opposingPieces = boardData.opposingPieces,
            visitedSquares = setOf(boardData.playerPieceLocus),
            movesRemaining = requiredMoves,
            currentScore = 0,
            level = 1,
            isTrainingMode = isTrainingMode,
        )
    }

    override fun getState(): GameEngineState {
        return state ?: throw IllegalStateException("Game not started")
    }

    override fun makeMove(to: Locus): MoveResult {
        val currentState = state ?: throw IllegalStateException("Game not started")
        val currentBoard = board ?: throw IllegalStateException("Board not initialized")

        if (currentState.isGameOver) {
            return MoveResult.Invalid
        }

        // Check if this is a valid move for the player's piece
        val validMoves = moveValidator.getValidMoves(
            piece = currentState.playerPiece,
            side = Side.WHITE,
            from = currentState.playerPieceLocus,
            board = currentBoard,
            blockers = currentState.visitedSquares + currentState.opposingPieces.keys
        )

        if (to !in validMoves) {
            return MoveResult.Invalid
        }

        // Move the piece first so the player's old position no longer blocks attack lines
        currentBoard.move(currentState.playerPieceLocus, to, Side.WHITE)

        // Check if the destination is under attack on the post-move board
        val isUnderAttack = moveValidator.isSquareUnderAttack(
            square = to,
            attackingSide = Side.BLACK,
            board = currentBoard
        )

        if (isUnderAttack) {
            state = currentState.copy(isGameOver = true, wasCaptured = true)
            return MoveResult.Captured
        }

        val newVisitedSquares = currentState.visitedSquares + to
        val newMovesRemaining = currentState.movesRemaining - 1

        if (newMovesRemaining <= 0) {
            // Level complete - advance to next level
            return advanceToNextLevel(currentState)
        } else {
            // Continue current level
            val newState = currentState.copy(
                playerPieceLocus = to,
                visitedSquares = newVisitedSquares,
                movesRemaining = newMovesRemaining
            )
            state = newState
            return MoveResult.Success(newState)
        }
    }

    override fun reset() {
        state = null
        board = null
    }

    private fun advanceToNextLevel(currentState: GameEngineState): MoveResult {
        val newLevel = currentState.level + 1
        val newScore = currentState.currentScore + 1

        // Calculate next piece and moves based on level
        val (nextPiece, nextMoves) = calculateLevelConfig(
            level = newLevel,
            isTrainingMode = currentState.isTrainingMode,
            piece = currentState.playerPiece
        )

        // Increase opposing pieces by 1 each level
        val opposingPieceCount = (INITIAL_OPPOSING_PIECES + (newLevel - 1))
            .coerceAtMost(MAX_OPPOSING_PIECES)

        val boardData = generateBoard(
            piece = nextPiece,
            requiredMoves = nextMoves,
            opposingPieceCount = opposingPieceCount
        )
        board = boardData.board

        val newState = GameEngineState(
            playerPiece = nextPiece,
            playerPieceLocus = boardData.playerPieceLocus,
            board = boardData.board,
            opposingPieces = boardData.opposingPieces,
            visitedSquares = setOf(boardData.playerPieceLocus),
            movesRemaining = nextMoves,
            currentScore = newScore,
            level = newLevel,
            isTrainingMode = currentState.isTrainingMode,
        )
        state = newState

        return MoveResult.LevelComplete(newState)
    }

    private fun calculateLevelConfig(
        level: Int,
        isTrainingMode: Boolean,
        piece: Piece
    ): Pair<Piece, Int> {
        return if (isTrainingMode) {
            // In training mode, always use the selected piece
            // Increase moves every 4 levels
            val moves = ((level - 1) / 4) + 1
            piece to moves.coerceAtMost(MAX_REQUIRED_MOVES)
        } else {
            // Non-training mode: cycle through pieces, then increase moves
            // Level 1-4: 1 move each for Bishop, Rook, Knight, Queen
            // Level 5-8: 2 moves each
            // etc.
            val pieceOrder = listOf(Piece.Bishop, Piece.Rook, Piece.Knight, Piece.Queen)
            val pieceIndex = (level - 1) % pieceOrder.size
            val moves = ((level - 1) / pieceOrder.size) + 1
            pieceOrder[pieceIndex] to moves.coerceAtMost(MAX_REQUIRED_MOVES)
        }
    }

    companion object {
        private const val INITIAL_OPPOSING_PIECES = 2
        private const val MAX_OPPOSING_PIECES = 10
        private const val MAX_REQUIRED_MOVES = 6
    }
}
