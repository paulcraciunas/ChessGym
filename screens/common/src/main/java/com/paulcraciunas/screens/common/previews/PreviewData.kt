package com.paulcraciunas.screens.common.previews

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.CapturedPieces
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.SessionResult

class PreviewData {
    fun whitePuzzleData() = BoardState(
        player = Side.WHITE,
        rating = 1442,
        id = 42,
        boardData = BoardViewData.default(),
        promotion = null,
        movePlayed = false,
        captured = CapturedPieces(
            byPlayer = listOf(Piece.Knight, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
        outcome = null,
    )

    fun blackPuzzleData() = BoardState(
        player = Side.BLACK,
        rating = 1442,
        id = 84,
        boardData = BoardViewData.default(),
        promotion = null,
        movePlayed = false,
        captured = CapturedPieces(
            byPlayer = listOf(Piece.Queen, Piece.Rook, Piece.Pawn, Piece.Pawn, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
        outcome = null,
    )

    fun whiteGameData() = BoardState(
        player = Side.WHITE,
        rating = null,
        id = null,
        boardData = BoardViewData.default(),
        promotion = null,
        movePlayed = false,
        captured = CapturedPieces(
            byPlayer = listOf(Piece.Knight, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
        outcome = null,
    )

    fun blackGameData() = BoardState(
        player = Side.BLACK,
        rating = null,
        id = null,
        boardData = BoardViewData.default(),
        promotion = null,
        movePlayed = false,
        captured = CapturedPieces(
            byPlayer = listOf(Piece.Queen, Piece.Rook, Piece.Pawn, Piece.Pawn, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
        outcome = null,
    )

    fun fewResults() = listOf(
        SessionResult(id = 1, rating = 1200, outcome = Outcome.Won),
        SessionResult(id = 2, rating = 1250, outcome = Outcome.Won),
        SessionResult(id = 3, rating = 1300, outcome = Outcome.Lost),
    )

    fun manyResults() = listOf(
        SessionResult(id = 1, rating = 1200, outcome = Outcome.Won),
        SessionResult(id = 2, rating = 1250, outcome = Outcome.Won),
        SessionResult(id = 3, rating = 1300, outcome = Outcome.Won),
        SessionResult(id = 4, rating = 1342, outcome = Outcome.Won),
        SessionResult(id = 5, rating = 1355, outcome = Outcome.Won),
        SessionResult(id = 6, rating = 1379, outcome = Outcome.Won),
        SessionResult(id = 7, rating = 1398, outcome = Outcome.Won),
        SessionResult(id = 8, rating = 1414, outcome = Outcome.Won),
        SessionResult(id = 9, rating = 1449, outcome = Outcome.Won),
        SessionResult(id = 10, rating = 1488, outcome = Outcome.Lost),
    )
}
