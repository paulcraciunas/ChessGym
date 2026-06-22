package com.paulcraciunas.domain.api.analysis

import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.Evaluation

data class MoveAnalysis(
    val moveIndex: Int,
    val fen: String,
    val evaluation: Evaluation,
    val centipawnLoss: Int,
    val classification: MoveClassification,
    val bestMove: EngineMove?,
)
