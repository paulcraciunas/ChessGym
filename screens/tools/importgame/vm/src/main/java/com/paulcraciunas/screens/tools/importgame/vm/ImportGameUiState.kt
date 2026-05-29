package com.paulcraciunas.screens.tools.importgame.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.CapturedPieces
import com.paulcraciunas.screens.common.model.PlayableData
import com.paulcraciunas.screens.common.model.Promotion

@Immutable
data class ImportGameUiState(
    val data: PlayableData = PlayableData(
        rating = null,
        player = Side.WHITE,
        id = null,
        boardData = BoardViewData.empty(),
        captured = CapturedPieces(byOpponent = "", byPlayer = ""),
        isOver = false,
        outcome = null,
    ),
    val promotion: Promotion? = null,
    val showImportDialog: ImportType? = null,
    val importType: ImportType? = null, // Keeps track of already imported game
    val importError: String? = null,
    val isGameLoaded: Boolean = false,
    val canNavigateBack: Boolean = false,
    val canNavigateForward: Boolean = false,
)

enum class ImportType { FEN, PGN }
