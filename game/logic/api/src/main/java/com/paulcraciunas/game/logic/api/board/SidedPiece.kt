package com.paulcraciunas.game.logic.api.board

import com.paulcraciunas.game.logic.api.Side

enum class SidedPiece(val side: Side, val piece: Piece) {
    WhitePawn(Side.WHITE, Piece.Pawn),
    WhiteKnight(Side.WHITE, Piece.Knight),
    WhiteBishop(Side.WHITE, Piece.Bishop),
    WhiteRook(Side.WHITE, Piece.Rook),
    WhiteQueen(Side.WHITE, Piece.Queen),
    WhiteKing(Side.WHITE, Piece.King),

    BlackPawn(Side.BLACK, Piece.Pawn),
    BlackKnight(Side.BLACK, Piece.Knight),
    BlackBishop(Side.BLACK, Piece.Bishop),
    BlackRook(Side.BLACK, Piece.Rook),
    BlackQueen(Side.BLACK, Piece.Queen),
    BlackKing(Side.BLACK, Piece.King);

    companion object {
        private val lookup: Array<Array<SidedPiece>> = Array(Side.entries.size) { s ->
            Array(Piece.entries.size) { p ->
                entries.first { it.side.ordinal == s && it.piece.ordinal == p }
            }
        }
        fun of(side: Side, piece: Piece): SidedPiece = lookup[side.ordinal][piece.ordinal]
    }
}
