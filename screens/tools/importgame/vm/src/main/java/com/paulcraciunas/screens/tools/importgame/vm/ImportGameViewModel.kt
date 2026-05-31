package com.paulcraciunas.screens.tools.importgame.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.GameSessionFactory
import com.paulcraciunas.screens.data.SessionSettings
import com.paulcraciunas.serializer.api.SerializeException
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import com.paulcraciunas.serializer.di.SerializerPgn
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImportGameViewModel @Inject constructor(
    @param:SerializerFen private val fenSerializer: Serializer,
    @param:SerializerPgn private val pgnSerializer: Serializer,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportGameUiState())
    val uiState: StateFlow<ImportGameUiState> = _uiState.asStateFlow()

    private var factory = GameSessionFactory()
    private var session = factory.get()

    init {
        session.bindSettings(viewModelScope, appSettingsRepository.appSettings.map {
            SessionSettings(it.autoPromote, it.enableAnimations)
        })
    }

    fun onFenClicked() = _uiState.update { it.copy(showImportDialog = ImportType.FEN, importError = null) }
    fun onPgnClicked() = _uiState.update { it.copy(showImportDialog = ImportType.PGN, importError = null) }
    fun onDismissDialog() = _uiState.update { it.copy(showImportDialog = null, importError = null) }

    fun onImport(text: String) {
        val importType = _uiState.value.showImportDialog ?: return
        try {
            val importedGame = serializer(importType).from(text)
            factory = GameSessionFactory()
            session = factory.get()
            viewModelScope.launch {
                factory.load(viewModelScope, importedGame, importedGame.info.turn)
                _uiState.update {
                    it.copy(
                        data = session.currentState,
                        showImportDialog = null,
                        importType = importType,
                        importError = null,
                        isGameLoaded = true,
                        canNavigateBack = session.canUndo(),
                        canNavigateForward = session.canReplay(),
                    )
                }
            }
        } catch (e: SerializeException) {
            Timber.w(e, "Failed to import game from: $text")
            _uiState.update { it.copy(importError = e.message) }
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Failed to import game from: $text")
            _uiState.update { it.copy(importError = e.message) }
        }
    }

    fun onSquareClicked(locus: Locus) {
        if (_uiState.value.importType == ImportType.PGN || !_uiState.value.isGameLoaded) return
        session.onClick(locus)
        updateState(session.currentState)
    }

    fun onPromote(to: Piece) {
        session.promoteIfPending(to)
        updateState(session.currentState)
    }

    fun onJumpToStart() = navigate { session.undoAll() }
    fun onPreviousMove() = navigate { session.undoLast() }
    fun onNextMove() = navigate { session.replayNext() }
    fun onJumpToEnd() = navigate { session.replayAll() }

    private fun updateState(data: BoardState) = _uiState.update {
        it.copy(
            data = data,
            canNavigateBack = session.canUndo(),
            canNavigateForward = session.canReplay(),
        )
    }

    private fun navigate(action: () -> Unit) {
        action()
        updateState(session.currentState)
    }

    private fun serializer(importType: ImportType): Serializer = when (importType) {
        ImportType.FEN -> fenSerializer
        ImportType.PGN -> pgnSerializer
    }
}
