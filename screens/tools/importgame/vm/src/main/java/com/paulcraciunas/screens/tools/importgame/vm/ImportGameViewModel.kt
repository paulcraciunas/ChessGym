package com.paulcraciunas.screens.tools.importgame.vm

import androidx.lifecycle.ViewModel
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewDataBuilder
import com.paulcraciunas.serializer.api.SerializeException
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import com.paulcraciunas.serializer.di.SerializerPgn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ImportGameViewModel @Inject constructor(
    @param:SerializerFen private val fenSerializer: Serializer,
    @param:SerializerPgn private val pgnSerializer: Serializer,
) : ViewModel(), ImportGameScreenInteractor {

    private val _uiState = MutableStateFlow(ImportGameUiState())
    val uiState: StateFlow<ImportGameUiState> = _uiState.asStateFlow()

    internal val moveHistory = MoveHistory(fenSerializer)
    private var game: Game? = null

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
            if (importedGame.state == Game.GameState.Ready) {
                importedGame.start()
            }

            when (dialogType) {
                ImportType.FEN -> {
                    game = importedGame
                    moveHistory.initForFen(importedGame, text)
                }
                ImportType.PGN -> {
                    game = importedGame
                    moveHistory.initForPgn(importedGame, text)
                }
            }

            _uiState.value = currentState.copy(
                boardData = moveHistory.currentSnapshot,
                isGameLoaded = true,
                importDialogType = null,
                importError = null,
                selectedSquare = null,
                legalMoves = emptyList(),
                playerSide = importedGame.info.turn,
                currentMoveIndex = moveHistory.currentIndex,
                totalMoves = moveHistory.totalMoves,
                importSource = dialogType,
            )
        } catch (e: SerializeException) {
            _uiState.value = currentState.copy(importError = e.message)
        } catch (e: IllegalArgumentException) {
            _uiState.value = currentState.copy(importError = e.message)
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

        var currentGame = game ?: return
        if (currentGame.state is Game.GameState.Finished) return

        if (!moveHistory.isAtLatestPosition) {
            currentGame = moveHistory.truncateAndReconstruct() ?: return
            game = currentGame
            updateNavigationState()
        }

        val currentlySelected = currentState.selectedSquare
        if (currentlySelected != null) {
            handleMoveAttempt(currentlySelected, locus, currentGame)
        } else {
            handleSelection(locus, currentGame)
        }
    }

    override fun onPromote(to: Piece) {
        val currentGame = game ?: return
        val currentState = _uiState.value
        val pending = currentState.pendingPromotion ?: return

        val ply = currentGame.plies(pending.from).find { it.to == pending.to }
        if (ply != null) {
            ply.promote(to)
            currentGame.play(ply)
            moveHistory.recordMove(currentGame, pending.from, pending.to, to)
            _uiState.value = currentState.copy(
                boardData = BoardViewDataBuilder.fromBoard(currentGame.board),
                selectedSquare = null,
                legalMoves = emptyList(),
                pendingPromotion = null,
                playerSide = currentGame.info.turn,
                currentMoveIndex = moveHistory.currentIndex,
                totalMoves = moveHistory.totalMoves,
            )
        } else {
            _uiState.value = currentState.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
                pendingPromotion = null,
            )
        }
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

    private fun updateNavigationState() {
        _uiState.value = _uiState.value.copy(
            boardData = moveHistory.currentSnapshot,
            currentMoveIndex = moveHistory.currentIndex,
            totalMoves = moveHistory.totalMoves,
            selectedSquare = null,
            legalMoves = emptyList(),
        )
    }

    private fun handleSelection(locus: Locus, currentGame: Game) {
        val plies = currentGame.plies(locus)
        if (plies.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
            )
        } else {
            _uiState.value = _uiState.value.copy(
                selectedSquare = locus,
                legalMoves = plies.map { it.to },
            )
        }
    }

    private fun handleMoveAttempt(from: Locus, to: Locus, currentGame: Game) {
        val ply = currentGame.plies(from).find { it.to == to }
        if (ply == null) {
            handleSelection(to, currentGame)
            return
        }

        if (ply.isPromotion()) {
            _uiState.value = _uiState.value.copy(
                pendingPromotion = PendingPromotion(from = from, to = to),
                selectedSquare = null,
                legalMoves = emptyList(),
            )
            return
        }

        currentGame.play(ply)
        moveHistory.recordMove(currentGame, from, to, promotionPiece = null)
        _uiState.value = _uiState.value.copy(
            boardData = BoardViewDataBuilder.fromBoard(currentGame.board),
            selectedSquare = null,
            legalMoves = emptyList(),
            playerSide = currentGame.info.turn,
            currentMoveIndex = moveHistory.currentIndex,
            totalMoves = moveHistory.totalMoves,
        )
    }
}
