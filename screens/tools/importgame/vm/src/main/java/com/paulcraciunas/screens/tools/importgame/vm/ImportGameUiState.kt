package com.paulcraciunas.screens.tools.importgame.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.previews.SampleBoardViewData

data class ImportGameUiState(
    val boardData: BoardViewData = SampleBoardViewData.emptyBoard(),
    val pendingPromotion: PendingPromotion? = null,
    val importDialogType: ImportType? = null,
    val importError: String? = null,
    val isGameLoaded: Boolean = false,
    val orientation: Side = Side.WHITE,
    val playerSide: Side = Side.WHITE,
    val currentMoveIndex: Int = 0,
    val totalMoves: Int = 0,
    val importSource: ImportType? = null,
)

enum class ImportType { FEN, PGN }

data class PendingPromotion(val from: Locus, val to: Locus)
