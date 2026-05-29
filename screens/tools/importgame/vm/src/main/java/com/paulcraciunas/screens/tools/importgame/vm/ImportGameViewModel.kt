package com.paulcraciunas.screens.tools.importgame.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardInteractionHelper
import com.paulcraciunas.screens.common.model.ClickResult
import com.paulcraciunas.screens.common.model.GameNavigation
import com.paulcraciunas.screens.common.model.GamePlayableBoard
import com.paulcraciunas.screens.common.model.PlayableData
import com.paulcraciunas.serializer.api.SerializeException
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import com.paulcraciunas.serializer.di.SerializerPgn
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportGameViewModel @Inject constructor(
    @param:SerializerFen private val fenSerializer: Serializer,
    @param:SerializerPgn private val pgnSerializer: Serializer,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportGameUiState())
    val uiState: StateFlow<ImportGameUiState> = _uiState.asStateFlow()

    private val helper = BoardInteractionHelper(navigation = GameNavigation())

    init {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { settings ->
                helper.autoPromote = settings.autoPromote
            }
        }
    }

    fun onFenClicked() = _uiState.update { it.copy(showImportDialog = ImportType.FEN, importError = null) }
    fun onPgnClicked() = _uiState.update { it.copy(showImportDialog = ImportType.PGN, importError = null) }
    fun onDismissDialog() = _uiState.update { it.copy(showImportDialog = null, importError = null) }
    fun onImport(text: String) {
        _uiState.update {
            if (it.showImportDialog == null) it
            else {
                try {
                    val importedGame = serializer(it.showImportDialog).from(text)
                    it.copy(
                        data = helper.load(GamePlayableBoard(importedGame, importedGame.info.turn)),
                        promotion = null,
                        showImportDialog = null,
                        importType = it.showImportDialog,
                        importError = null,
                        isGameLoaded = true,
                        canNavigateBack = helper.canUndo(),
                        canNavigateForward = helper.canReplay(),
                    )
                } catch (e: SerializeException) {
                    Timber.w(e, "Failed to import game from: $text")
                    it.copy(importError = e.message)
                } catch (e: IllegalArgumentException) {
                    Timber.w(e, "Failed to import game from: $text")
                    it.copy(importError = e.message)
                }
            }
        }
    }

    fun onSquareClicked(locus: Locus) {
        if (_uiState.value.importType == ImportType.PGN || !_uiState.value.isGameLoaded) return
        applyResult(helper.handleSquareClick(locus))
    }

    fun onPromote(to: Piece) {
        _uiState.value.promotion?.let {
            applyResult(helper.promote(to, it.at))
        }
    }

    fun onJumpToStart() = navigate { helper.undoAll() }
    fun onPreviousMove() = navigate { helper.undoLast() }
    fun onNextMove() = navigate { helper.replayNext() }
    fun onJumpToEnd() = navigate { helper.replayAll() }

    private fun applyResult(result: ClickResult) = _uiState.update {
        it.copy(
            data = result.data,
            promotion = result.promotion,
            canNavigateBack = helper.canUndo(),
            canNavigateForward = helper.canReplay(),
        )
    }

    private fun navigate(gameDataSource: () -> PlayableData) = _uiState.update {
        it.copy(
            data = gameDataSource(),
            promotion = null,
            canNavigateBack = helper.canUndo(),
            canNavigateForward = helper.canReplay(),
        )
    }

    private fun serializer(importType: ImportType): Serializer = when (importType) {
        ImportType.FEN -> fenSerializer
        ImportType.PGN -> pgnSerializer
    }
}
