package com.paulcraciunas.game.engine.api

data class AnalysisResult(
    val depth: Int,
    val evaluation: Evaluation,
    val lines: List<EngineLine>,
)
