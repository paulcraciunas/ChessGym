package com.paulcraciunas.screens.tools.importgame.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.logic.builders.Builders
import com.paulcraciunas.screens.common.model.GameData
import com.paulcraciunas.screens.common.model.GameViewModelHelper
import com.paulcraciunas.serializer.api.SerializeException
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import com.paulcraciunas.serializer.di.SerializerPgn
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportGameViewModel @Inject constructor(
    @param:SerializerFen private val fenSerializer: Serializer,
    @param:SerializerPgn private val pgnSerializer: Serializer,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel(), ImportGameScreenInteractor {

    private val _uiState = MutableStateFlow(ImportGameUiState())
    val uiState: StateFlow<ImportGameUiState> = _uiState.asStateFlow()

    private val helper = GameViewModelHelper(gameInteractor = Builders.gameInteractor())

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { settings ->
                helper.autoPromote = settings.autoPromote
            }
        }
    }

    override fun onFenClicked() {
        _uiState.value = _uiState.value.copy(
            importDialogType = ImportType.FEN,
            importError = null,
        )
    }

    override fun onPgnClicked() {
        _uiState.value = _uiState.value.copy(
            importDialogType = ImportType.PGN,
            importError = null,
        )
    }

    override fun onImport(text: String) {
        val currentState = _uiState.value
        val dialogType = currentState.importDialogType ?: return

        val serializer = when (dialogType) {
            ImportType.FEN -> fenSerializer
            ImportType.PGN -> pgnSerializer
        }

        try {
            val importedGame = serializer.from(text)
            val gameData = helper.load(importedGame, importedGame.info.turn)

            _uiState.value = currentState.copy(
                boardData = gameData.boardData,
                isGameLoaded = true,
                importDialogType = null,
                importError = null,
                pendingPromotion = null,
                orientation = importedGame.info.turn,
                playerSide = importedGame.info.turn,
                currentMoveIndex = helper.game.currentMoveIndex,
                totalMoves = helper.game.historySize,
                importSource = dialogType,
            )
        } catch (e: SerializeException) {
            _uiState.value = currentState.copy(importError = e.message)
            Timber.w(e, "Failed to import game from: $text")
        } catch (e: IllegalArgumentException) {
            _uiState.value = currentState.copy(importError = e.message)
            Timber.w(e, "Failed to import game from: $text")
        }
    }

    override fun onDismissDialog() {
        _uiState.value = _uiState.value.copy(
            importDialogType = null,
            importError = null,
        )
    }

    override fun onSquareClicked(locus: Locus) {
        val currentState = _uiState.value
        if (currentState.importSource == ImportType.PGN) return
        if (!currentState.isGameLoaded) return

        val result = helper.handleSquareClick(locus)
        applyResult(result)
    }

    override fun onPromote(to: Piece) {
        val pending = _uiState.value.pendingPromotion ?: return

        val result = helper.promote(to, pending.to)
        _uiState.value = _uiState.value.copy(
            boardData = result.data.boardData,
            pendingPromotion = null,
            playerSide = result.data.player,
            currentMoveIndex = helper.game.currentMoveIndex,
            totalMoves = helper.game.historySize,
        )
    }

    override fun onJumpToStart() = navigate { helper.undoAll() }
    override fun onPreviousMove() = navigate { helper.undoLast() }
    override fun onNextMove() = navigate { helper.replayNext() }
    override fun onJumpToEnd() = navigate { helper.replayAll() }

    private fun applyResult(result: GameViewModelHelper.OnSquareClick) {
        val promotion = result.promotion
        val moveFrom = result.moveFrom
        if (promotion != null && moveFrom != null) {
            _uiState.value = _uiState.value.copy(
                boardData = result.data.boardData,
                pendingPromotion = PendingPromotion(
                    from = moveFrom,
                    to = promotion.at,
                ),
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            boardData = result.data.boardData,
            playerSide = result.data.player,
            currentMoveIndex = helper.game.currentMoveIndex,
            totalMoves = helper.game.historySize,
        )
    }

    private fun navigate(gameDataSource: () -> GameData) {
        val data = gameDataSource()
        _uiState.value = _uiState.value.copy(
            boardData = data.boardData,
            currentMoveIndex = helper.game.currentMoveIndex,
            totalMoves = helper.game.historySize,
        )
    }
}
