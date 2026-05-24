package com.paulcraciunas.screens.common.model.v2

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap

@Immutable
data class PuzzleData2(
    val rating: Int,
    val player: Side,
    val id: Int?,
    val boardData: BoardViewData2,
    val captured: PersistentMap<Side, PersistentList<Piece>>,
)
