package com.paulcraciunas.game.logic.api

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

    fun isDraw(): Boolean = this != CheckMate && this != Resigned
}
