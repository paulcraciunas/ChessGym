package com.paulcraciunas.game.engine.api

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

data class EngineMove(
    val from: Locus,
    val to: Locus,
    val promotion: Piece? = null,
)
