package com.paulcraciunas.chessgym.game

//TODO Paul: rename to result
enum class Ending {
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
