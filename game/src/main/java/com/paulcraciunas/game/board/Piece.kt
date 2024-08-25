package com.paulcraciunas.game.board

enum class Piece(private val algebraic: String) {
    Pawn(""),
    Rook("R"),
    Knight("N"),
    Bishop("B"),
    Queen("Q"),
    King("K");

    fun alg(): String = algebraic
}
