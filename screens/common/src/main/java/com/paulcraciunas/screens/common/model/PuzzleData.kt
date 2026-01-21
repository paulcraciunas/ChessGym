package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece

data class PuzzleData(
    val rating: Int,
    val player: Side,
    val boardData: BoardViewData,
    val captured: Map<Side, List<Piece>>,
)
