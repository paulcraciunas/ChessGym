package com.paulcraciunas.screens.tools.importgame.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.data.BoardState

@Immutable
data class ImportGameUiState(
    val data: BoardState = BoardState.empty,
    val showImportDialog: ImportType? = null,
    val importType: ImportType? = null,
    val importError: String? = null,
    val isGameLoaded: Boolean = false,
    val canNavigateBack: Boolean = false,
    val canNavigateForward: Boolean = false,
)

enum class ImportType { FEN, PGN }
