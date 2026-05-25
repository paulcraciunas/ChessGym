package com.paulcraciunas.screens.common.model.v2

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece

@Immutable
data class PuzzleData2(
    val rating: Int,
    val player: Side,
    val id: Int?,
    val boardData: BoardViewData2,
    val captured: Map<Side, List<Piece>>,
) {
    fun playerCaptured(): List<Piece> = captured[player]!!
    fun otherCaptured(): List<Piece> = captured[player.other()]!!
}
