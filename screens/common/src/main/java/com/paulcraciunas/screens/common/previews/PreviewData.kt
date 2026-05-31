package com.paulcraciunas.screens.common.previews

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.CapturedPieces
import com.paulcraciunas.screens.data.PuzzleResult

class PreviewData {
    fun whitePuzzleData() = BoardState(
        rating = 1400,
        player = Side.WHITE,
        id = 42,
        boardData = BoardViewData.default(),
        promotion = null,
        movePlayed = false,
        captured = CapturedPieces(
            byPlayer = listOf(Piece.Knight, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
        isOver = false,
        interactive = true,
        outcome = null,
    )

    fun blackPuzzleData() = BoardState(
        rating = 1350,
        player = Side.BLACK,
        id = 84,
        boardData = BoardViewData.default(),
        promotion = null,
        movePlayed = false,
        captured = CapturedPieces(
            byPlayer = listOf(Piece.Queen, Piece.Rook, Piece.Pawn, Piece.Pawn, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
        isOver = false,
        interactive = true,
        outcome = null,
    )

    fun whiteGameData() = BoardState(
        rating = 1442,
        player = Side.WHITE,
        id = null,
        boardData = BoardViewData.default(),
        promotion = null,
        movePlayed = false,
        captured = CapturedPieces(
            byPlayer = listOf(Piece.Knight, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
        isOver = false,
        interactive = true,
        outcome = null,
    )

    fun blackGameData() = BoardState(
        rating = 1350,
        player = Side.BLACK,
        id = null,
        boardData = BoardViewData.default(),
        promotion = null,
        movePlayed = false,
        captured = CapturedPieces(
            byPlayer = listOf(Piece.Queen, Piece.Rook, Piece.Pawn, Piece.Pawn, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
        isOver = false,
        interactive = true,
        outcome = null,
    )

    fun fewResults() = listOf(
        PuzzleResult(id = 1, rating = 1200, success = true),
        PuzzleResult(id = 2, rating = 1250, success = true),
        PuzzleResult(id = 3, rating = 1300, success = false),
    )

    fun manyResults() = listOf(
        PuzzleResult(id = 1, rating = 1200, success = true),
        PuzzleResult(id = 2, rating = 1250, success = true),
        PuzzleResult(id = 3, rating = 1300, success = true),
        PuzzleResult(id = 4, rating = 1342, success = true),
        PuzzleResult(id = 5, rating = 1355, success = true),
        PuzzleResult(id = 6, rating = 1379, success = true),
        PuzzleResult(id = 7, rating = 1398, success = true),
        PuzzleResult(id = 8, rating = 1414, success = true),
        PuzzleResult(id = 9, rating = 1449, success = true),
        PuzzleResult(id = 10, rating = 1488, success = false),
    )
}
