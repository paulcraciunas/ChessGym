package com.paulcraciunas.screens.common.previews

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.GameViewModelHelper.GameData2
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleResult

class PreviewData {
    fun whitePuzzleData() = PuzzleData(
        rating = 1400,
        player = Side.WHITE,
        id = 42,
        boardData = BoardViewData.default(),
        captured = PuzzleData.Captured(
            byPlayer = listOf(Piece.Knight, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
    )

    fun blackPuzzleData() = PuzzleData(
        rating = 1350,
        player = Side.BLACK,
        id = 84,
        boardData = BoardViewData.default(),
        captured = PuzzleData.Captured(
            byPlayer = listOf(Piece.Queen, Piece.Rook, Piece.Pawn, Piece.Pawn, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
    )

    fun whiteGameData() = GameData2(
        rating = 1442,
        player = Side.WHITE,
        boardData = BoardViewData.default(),
        captured = GameData2.GameCaptured(
            byPlayer = listOf(Piece.Knight, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
    )

    fun blackGameData() = GameData2(
        rating = 1350,
        player = Side.BLACK,
        boardData = BoardViewData.default(),
        captured = GameData2.GameCaptured(
            byPlayer = listOf(Piece.Queen, Piece.Rook, Piece.Pawn, Piece.Pawn, Piece.Pawn).joinToString("") { it.unicode },
            byOpponent = listOf(Piece.Rook, Piece.Bishop, Piece.Pawn).joinToString("") { it.unicode },
        ),
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
