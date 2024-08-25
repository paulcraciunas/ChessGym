package com.paulcraciunas.chessgym.game

enum class Result {
    CheckMate,
    StaleMate,
    Resigned,
    DrawByAgreement,
    DrawByRepetition,
    DrawByMoveRule,
    DrawByInsufficientMaterial;

    fun algebraic(turn: Side): String = when (this) {
        CheckMate, Resigned -> if (turn == Side.WHITE) "0-1" else "1-0"
        else -> "1/2-1/2"
    }
}
