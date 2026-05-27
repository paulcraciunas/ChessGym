package com.paulcraciunas.game.logic.api.board

enum class Piece(
    private val algebraic: String,
    val code: Int,
    val unicode: String,
    val defaultCount: Int
) {
    Pawn(algebraic = "", code = 0, unicode = "♟", defaultCount = 8),
    Rook(algebraic = "R", code = 1, unicode = "♜", defaultCount = 2),
    Knight(algebraic = "N", code = 2, unicode = "♞", defaultCount = 2),
    Bishop(algebraic = "B", code = 3, unicode = "♝", defaultCount = 2),
    Queen(algebraic = "Q", code = 4, unicode = "♛", defaultCount = 1),
    King(algebraic = "K", code = 5, unicode = "♚", defaultCount = 1);

    fun alg(): String = algebraic

    companion object {
        fun fromCode(code: Int): Piece =
            if (code in 0..5) Piece.entries[code]
            else throw IllegalArgumentException("Wrong decimal value. Expecting [0 - 5]")

        fun fromAlgebraic(alg: String): Piece =
            Piece.entries.find { it.alg().equals(alg, ignoreCase = true) }
                ?: throw IllegalArgumentException("Wrong algebraic value. Expecting [RBNQK ]")
    }
}
