package com.paulcraciunas.screens.tools.importgame.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.GameViewModelHelper
import com.paulcraciunas.screens.common.model.GameViewModelHelper.GameData2

@Immutable
data class ImportGameUiState(
    val data: GameData2 = GameData2(
        rating = null,
        player = Side.WHITE,
        boardData = BoardViewData.empty(),
        captured = GameData2.GameCaptured(byOpponent = "", byPlayer = "")
    ),
    val promotion: GameViewModelHelper.GamePromotion? = null,
    val showImportDialog: ImportType? = null,
    val importType: ImportType? = null, // Keeps track of already imported game
    val importError: String? = null,
    val isGameLoaded: Boolean = false,
    val canNavigateBack: Boolean = false,
    val canNavigateForward: Boolean = false,
)

enum class ImportType { FEN, PGN }
