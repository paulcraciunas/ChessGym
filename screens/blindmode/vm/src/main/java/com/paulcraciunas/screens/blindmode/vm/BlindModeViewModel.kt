package com.paulcraciunas.screens.blindmode.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.blindmode.BlindModeGameResult
import com.paulcraciunas.domain.api.blindmode.BlindModeOrchestrator
import com.paulcraciunas.domain.api.blindmode.EnginePlayResult
import com.paulcraciunas.domain.api.blindmode.OnBlindModeGameComplete
import com.paulcraciunas.domain.api.blindmode.PlayResult
import com.paulcraciunas.domain.api.blindmode.SelectionResult
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.algebraic
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
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
    private val timer: Timer,
    private val onComplete: OnBlindModeGameComplete,
) : ViewModel(), BlindModeScreenInteractor {

    private val _uiState = MutableStateFlow<BlindModeUiState>(BlindModeUiState.Setup())
    val uiState: StateFlow<BlindModeUiState> = _uiState.asStateFlow()

    private var isTrainingMode: Boolean = true
    private var selectedFrom: Locus? = null

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    override fun onTrainingModeToggled(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is BlindModeUiState.Setup) {
            isTrainingMode = enabled
            _uiState.value = currentState.copy(isTrainingMode = enabled)
        }
    }

    override fun onPlayClicked() {
        if (_uiState.value !is BlindModeUiState.Setup) return

        timer.start()
        viewModelScope.launch {
            orchestrator.startGame(elo = ChessEngine.DEFAULT_ELO)

            _uiState.value = BlindModeUiState.Playing(
                moveHistory = "",
                isRevealAvailable = true,
            )
        }
    }

    override fun onSquareClicked(locus: Locus) {
        val currentState = _uiState.value
        if (currentState !is BlindModeUiState.Playing || currentState.isThinking) return

        val currentlySelected = selectedFrom
        if (currentlySelected != null) {
            handleMoveAttempt(currentlySelected, locus, currentState)
        } else {
            handleSelection(locus, currentState)
        }
    }

    override fun onPromote(to: Piece) {
        // TODO Paul: handle Promotion
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
                boardData = BoardViewDataBuilder.fromBoard(orchestrator.board()),
                moveHistory = moveHistory,
            )

            delay(REVEAL_DURATION_MS)

            _uiState.value = BlindModeUiState.Playing(
                moveHistory = moveHistory,
                isRevealAvailable = false,
                selectedSquare = null,
                legalMoves = emptyList(),
            )
        }
    }

    override fun onPlayAgain() {
        val currentState = _uiState.value
        if (currentState !is BlindModeUiState.GameOver) return

        viewModelScope.launch {
            orchestrator.reset()
            selectedFrom = null
            _uiState.value = BlindModeUiState.Setup(isTrainingMode = isTrainingMode)
        }
    }

    private fun handleSelection(locus: Locus, currentState: BlindModeUiState.Playing) {
        when (val result = orchestrator.selectSquare(locus)) {
            is SelectionResult.PieceSelected -> {
                selectedFrom = result.locus
                _uiState.value = currentState.copy(
                    selectedSquare = result.locus,
                    legalMoves = result.legalMoves,
                )
            }
            is SelectionResult.NoPiece,
            is SelectionResult.WrongSide -> {
                selectedFrom = null
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
        selectedFrom = null
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
            is PlayResult.GameOver -> finishGame(result = result.result)
            is PlayResult.Invalid -> handleSelection(to, currentState)
        }
    }

    private fun requestEngineMove() {
        viewModelScope.launch {
            when (val result = orchestrator.requestEngineMove()) {
                is EnginePlayResult.Success -> {
                    _uiState.value = BlindModeUiState.Playing(
                        moveHistory = orchestrator.moveHistory().algebraic(),
                        selectedSquare = null,
                        legalMoves = emptyList(),
                        isRevealAvailable = (_uiState.value as? BlindModeUiState.Playing)?.isRevealAvailable ?: false,
                        isThinking = false,
                    )
                }
                is EnginePlayResult.GameOver -> finishGame(result = result.result)
            }
        }
    }

    private fun finishGame(result: Result) {
        val movesPlayed = orchestrator.moveHistory().size
        val uiResult = result.toUiResult()

        viewModelScope.launch {
            onComplete(
                BlindModeGameResult(
                    result = result,
                    isPlayerWin = uiResult == BlindModeUiState.GameResult.Win,
                    movesPlayed = movesPlayed,
                    timeSpentMillis = timer.elapsed(),
                    isTrainingMode = isTrainingMode,
                    opponentElo = ChessEngine.DEFAULT_ELO,
                )
            )
        }

        _uiState.value = BlindModeUiState.GameOver(
            boardData = BoardViewDataBuilder.fromBoard(orchestrator.board()),
            moveHistory = orchestrator.moveHistory().algebraic(),
            result = uiResult
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
