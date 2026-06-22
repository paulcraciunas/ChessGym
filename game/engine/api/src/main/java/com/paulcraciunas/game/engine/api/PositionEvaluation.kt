package com.paulcraciunas.game.engine.api

data class PositionEvaluation(
    val depth: Int,
    val evaluation: Evaluation,
    val bestMove: EngineMove?,
)
