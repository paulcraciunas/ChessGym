package com.paulcraciunas.screens.blindmode.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.blindmode.BlindModeGameResult
import com.paulcraciunas.domain.api.blindmode.BlindModeOrchestrator
import com.paulcraciunas.domain.api.blindmode.EnginePlayResult
import com.paulcraciunas.domain.api.blindmode.OnBlindModeGameComplete
import com.paulcraciunas.domain.api.blindmode.PlayResult
import com.paulcraciunas.domain.api.blindmode.SelectionResult
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.algebraic
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.controls.SideSelection
import com.paulcraciunas.screens.common.model.BoardViewDataBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlindModeViewModel @Inject constructor(
    private val orchestrator: BlindModeOrchestrator,
    private val onComplete: OnBlindModeGameComplete,
    private val timer: Timer,
    private val randomFactory: RandomFactory,
) : ViewModel(), BlindModeScreenInteractor {

    private val _uiState = MutableStateFlow<BlindModeUiState>(BlindModeUiState.Setup())
    val uiState: StateFlow<BlindModeUiState> = _uiState.asStateFlow()

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    override fun onTrainingModeToggled(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is BlindModeUiState.Setup) {
            _uiState.value = currentState.copy(isTrainingMode = enabled)
        }
    }

    override fun onSideSelected(side: SideSelection) {
        val currentState = _uiState.value
        if (currentState is BlindModeUiState.Setup) {
            _uiState.value = currentState.copy(selectedSide = side)
        }
    }

    override fun onPlayClicked() {
        val currentState = _uiState.value
        if (currentState !is BlindModeUiState.Setup) return

        val side = when (currentState.selectedSide) {
            SideSelection.WHITE -> Side.WHITE
            SideSelection.BLACK -> Side.BLACK
            SideSelection.RANDOM -> Side.fromCode(randomFactory.nextInt(0, 2))
        }
        timer.start()
        viewModelScope.launch {
            orchestrator.startGame(elo = ChessEngine.DEFAULT_ELO, side = side)

            _uiState.value = BlindModeUiState.Playing(
                isTrainingMode = currentState.isTrainingMode,
                selectedSide = currentState.selectedSide,
                playerSide = side,
            )

            if (side == Side.BLACK) {
                requestEngineMove()
            }
        }
    }

    override fun onSquareClicked(locus: Locus) {
        val currentState = _uiState.value
        if (currentState !is BlindModeUiState.Playing || currentState.isThinking) return

        val currentlySelected = currentState.selectedSquare
        if (currentlySelected != null) {
            handleMoveAttempt(currentlySelected, locus, currentState)
        } else {
            handleSelection(locus, currentState)
        }
    }

    override fun onPromote(to: Piece) {
        val currentState = _uiState.value
        if (currentState !is BlindModeUiState.Playing) return

        val pending = currentState.pendingPromotion ?: return

        when (val result = orchestrator.playMove(pending.from, pending.to, to)) {
            is PlayResult.Success -> {
                _uiState.value = currentState.copy(
                    moveHistory = orchestrator.moveHistory().algebraic(),
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    isThinking = true,
                    pendingPromotion = null,
                )
                requestEngineMove()
            }
            is PlayResult.GameOver -> finishGame(result = result.result)
            is PlayResult.PromotionRequired,
            is PlayResult.Invalid -> {
                _uiState.value = currentState.copy(
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    pendingPromotion = null,
                )
            }
        }
    }

    override fun onResign() {
        val currentState = _uiState.value
        if (currentState !is BlindModeUiState.Playing) return

        orchestrator.resign()
        finishGame(result = Result.Resigned)
    }

    override fun onReveal() {
        val currentState = _uiState.value
        if (currentState !is BlindModeUiState.Playing || !currentState.isRevealAvailable) return

        viewModelScope.launch {
            val moveHistory = orchestrator.moveHistory().algebraic()
            _uiState.value = BlindModeUiState.Revealing(
                isTrainingMode = currentState.isTrainingMode,
                selectedSide = currentState.selectedSide,
                playerSide = orchestrator.playerSide(),
                boardData = BoardViewDataBuilder.fromBoard(orchestrator.board()),
                moveHistory = moveHistory,
            )

            delay(REVEAL_DURATION_MS)

            _uiState.value = BlindModeUiState.Playing(
                isTrainingMode = currentState.isTrainingMode,
                selectedSide = currentState.selectedSide,
                moveHistory = moveHistory,
                playerSide = orchestrator.playerSide(),
                isRevealAvailable = currentState.isTrainingMode,
            )
        }
    }

    override fun onPlayAgain() {
        val currentState = _uiState.value
        if (currentState !is BlindModeUiState.GameOver) return

        viewModelScope.launch {
            orchestrator.reset()
            _uiState.value = BlindModeUiState.Setup(
                isTrainingMode = currentState.isTrainingMode,
                selectedSide = currentState.selectedSide,
            )
        }
    }

    override fun onBackPressed(): Boolean {
        val currentState = _uiState.value
        if (currentState !is BlindModeUiState.Playing) return false

        _uiState.value = currentState.copy(isAbandonDialogShown = true)
        return true
    }

    override fun onAbandonConfirmed() {
        onResign()
    }

    override fun onAbandonDismissed() {
        val currentState = _uiState.value
        if (currentState is BlindModeUiState.Playing) {
            _uiState.value = currentState.copy(isAbandonDialogShown = false)
        }
    }

    private fun handleSelection(locus: Locus, currentState: BlindModeUiState.Playing) {
        when (val result = orchestrator.selectSquare(locus)) {
            is SelectionResult.PieceSelected -> {
                _uiState.value = currentState.copy(
                    selectedSquare = result.locus,
                    legalMoves = result.legalMoves,
                )
            }
            is SelectionResult.NoPiece,
            is SelectionResult.WrongSide -> {
                _uiState.value = currentState.copy(
                    selectedSquare = null,
                    legalMoves = emptyList(),
                )
            }
        }
    }

    private fun handleMoveAttempt(
        from: Locus,
        to: Locus,
        currentState: BlindModeUiState.Playing,
    ) {
        when (val result = orchestrator.playMove(from, to)) {
            is PlayResult.Success -> {
                _uiState.value = currentState.copy(
                    moveHistory = orchestrator.moveHistory().algebraic(),
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    isThinking = true,
                )
                requestEngineMove()
            }
            is PlayResult.PromotionRequired -> {
                _uiState.value = currentState.copy(
                    pendingPromotion = BlindModeUiState.PendingPromotion(
                        from = from,
                        to = to,
                    ),
                    selectedSquare = null,
                    legalMoves = emptyList(),
                )
            }
            is PlayResult.GameOver -> finishGame(result = result.result)
            is PlayResult.Invalid -> handleSelection(to, currentState)
        }
    }

    private fun requestEngineMove() {
        viewModelScope.launch {
            val currentState = _uiState.value
            when (val result = orchestrator.requestEngineMove()) {
                is EnginePlayResult.Success -> {
                    _uiState.value = BlindModeUiState.Playing(
                        isTrainingMode = currentState.isTrainingMode,
                        selectedSide = currentState.selectedSide,
                        moveHistory = orchestrator.moveHistory().algebraic(),
                        playerSide = orchestrator.playerSide(),
                        isRevealAvailable = (currentState as? BlindModeUiState.Playing)
                            ?.isRevealAvailable ?: currentState.isTrainingMode,
                    )
                }
                is EnginePlayResult.GameOver -> finishGame(result = result.result)
            }
        }
    }

    private fun finishGame(result: Result) {
        val currentState = _uiState.value
        val movesPlayed = orchestrator.moveHistory().size
        val uiResult = result.toUiResult()

        viewModelScope.launch {
            onComplete(
                BlindModeGameResult(
                    result = result,
                    isPlayerWin = uiResult == BlindModeUiState.GameResult.Win,
                    movesPlayed = movesPlayed,
                    timeSpentMillis = timer.elapsed(),
                    isTrainingMode = currentState.isTrainingMode,
                    opponentElo = ChessEngine.DEFAULT_ELO,
                )
            )
        }

        _uiState.value = BlindModeUiState.GameOver(
            isTrainingMode = currentState.isTrainingMode,
            selectedSide = currentState.selectedSide,
            boardData = BoardViewDataBuilder.fromBoard(orchestrator.board()),
            moveHistory = orchestrator.moveHistory().algebraic(),
            result = uiResult,
        )
    }

    private fun Result.toUiResult(): BlindModeUiState.GameResult {
        val playerSide = orchestrator.playerSide()
        val lastPly = orchestrator.moveHistory().lastOrNull()
        return if (this == Result.CheckMate || this == Result.Resigned) {
            if (lastPly?.turn == playerSide) {
                BlindModeUiState.GameResult.Win
            } else {
                BlindModeUiState.GameResult.Loss
            }
        } else BlindModeUiState.GameResult.Draw
    }

    companion object {
        internal const val REVEAL_DURATION_MS = 3000L
    }
}
