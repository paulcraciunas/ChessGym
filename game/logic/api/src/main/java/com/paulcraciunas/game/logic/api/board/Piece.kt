package com.paulcraciunas.game.logic.api.board

enum class Piece(private val algebraic: String, val code: Int) {
    Pawn(algebraic = "", code = 0),
    Rook(algebraic = "R", code = 1),
    Knight(algebraic = "N", code = 2),
    Bishop(algebraic = "B", code = 3),
    Queen(algebraic = "Q", code = 4),
    King(algebraic = "K", code = 5);

    fun alg(): String = algebraic

    companion object {
        fun fromCode(code: Int): Piece =
            if (code in 0..5) Piece.entries[code]
            else throw IllegalArgumentException("Wrong decimal value. Expecting [0 - 5]")

        fun fromAlgebraic(alg: String): Piece =
            Piece.entries.find { it.alg() == alg } ?: throw IllegalArgumentException("Wrong algebraic value. Expecting [RBNQK ]")
    }
}
