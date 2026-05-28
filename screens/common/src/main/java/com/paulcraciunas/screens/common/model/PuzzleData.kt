package com.paulcraciunas.screens.common.model

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side

@Immutable
data class PuzzleData(
    val rating: Int,
    val player: Side,
    val id: Int?,
    val boardData: BoardViewData2,
    val captured: Captured,
) {
    @Immutable
    data class Captured(
        val byPlayer: String,
        val byOpponent: String,
    )
}
