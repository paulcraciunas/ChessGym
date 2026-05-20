package com.paulcraciunas.screens.tools.importgame.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.GameInteractor
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
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
    gameInteractor: GameInteractor,
) : ViewModel(), ImportGameScreenInteractor {

    private val _uiState = MutableStateFlow(ImportGameUiState())
    val uiState: StateFlow<ImportGameUiState> = _uiState.asStateFlow()

    private val helper = GameViewModelHelper(gameInteractor)
    internal val moveHistory = MoveHistory(fenSerializer)

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
            helper.load(importedGame, importedGame.info.turn)

            when (dialogType) {
                ImportType.FEN -> moveHistory.initForFen(importedGame, text)
                ImportType.PGN -> moveHistory.initForPgn(importedGame, text)
            }

            _uiState.value = currentState.copy(
                boardData = moveHistory.currentSnapshot,
                isGameLoaded = true,
                importDialogType = null,
                importError = null,
                pendingPromotion = null,
                orientation = importedGame.info.turn,
                playerSide = importedGame.info.turn,
                currentMoveIndex = moveHistory.currentIndex,
                totalMoves = moveHistory.totalMoves,
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

        if (!moveHistory.isAtLatestPosition) {
            val freshGame = moveHistory.truncateAndReconstruct()
            helper.load(freshGame, freshGame.info.turn)
            updateNavigationState()
        }

        val result = helper.handleSquareClick(locus)
        applyResult(result, destination = locus)
    }

    override fun onPromote(to: Piece) {
        val pending = _uiState.value.pendingPromotion ?: return

        val result = helper.promote(to, pending.to)
        moveHistory.recordMoveFromHelper(pending.from, pending.to, to, result.data.boardData)

        _uiState.value = _uiState.value.copy(
            boardData = result.data.boardData,
            pendingPromotion = null,
            playerSide = result.data.player,
            currentMoveIndex = moveHistory.currentIndex,
            totalMoves = moveHistory.totalMoves,
        )
    }

    override fun onJumpToStart() {
        moveHistory.jumpToStart()
        updateNavigationState()
    }

    override fun onPreviousMove() {
        moveHistory.previousMove()
        updateNavigationState()
    }

    override fun onNextMove() {
        moveHistory.nextMove()
        updateNavigationState()
    }

    override fun onJumpToEnd() {
        moveHistory.jumpToEnd()
        updateNavigationState()
    }

    private fun applyResult(result: GameViewModelHelper.OnSquareClick, destination: Locus) {
        val moveFrom = result.moveFrom
        val promotion = result.promotion
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

        if (result.movePlayed && moveFrom != null) {
            moveHistory.recordMoveFromHelper(moveFrom, destination, result.autoPromotedTo, result.data.boardData)
        }

        _uiState.value = _uiState.value.copy(
            boardData = result.data.boardData,
            playerSide = if (result.movePlayed) result.data.player.other() else result.data.player,
            currentMoveIndex = moveHistory.currentIndex,
            totalMoves = moveHistory.totalMoves,
        )
    }

    private fun updateNavigationState() {
        _uiState.value = _uiState.value.copy(
            boardData = moveHistory.currentSnapshot,
            currentMoveIndex = moveHistory.currentIndex,
            totalMoves = moveHistory.totalMoves,
        )
    }
}
