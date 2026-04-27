package com.paulcraciunas.game.engine.api

data class EngineLine(
    val rank: Int,
    val evaluation: Evaluation,
    val moves: List<String>,
)
