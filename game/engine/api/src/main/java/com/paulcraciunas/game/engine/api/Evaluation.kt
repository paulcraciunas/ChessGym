package com.paulcraciunas.game.engine.api

sealed class Evaluation {
    data class Centipawns(val value: Int) : Evaluation()
    data class Mate(val movesToMate: Int) : Evaluation()
}
