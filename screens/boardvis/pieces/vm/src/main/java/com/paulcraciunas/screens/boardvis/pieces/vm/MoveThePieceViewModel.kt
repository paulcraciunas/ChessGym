package com.paulcraciunas.screens.boardvis.pieces.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.GameEngineState
import com.paulcraciunas.domain.api.MoveResult
import com.paulcraciunas.domain.api.MoveThePieceGameEngine
import com.paulcraciunas.domain.api.MoveThePieceResult
import com.paulcraciunas.domain.api.OnMoveThePieceComplete
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoveThePieceViewModel @Inject constructor(
    private val gameEngine: MoveThePieceGameEngine,
    private val onComplete: OnMoveThePieceComplete,
    private val countdownTimer: CountdownTimer,
    private val userRepository: UserRepository,
) : ViewModel(), MoveThePieceScreenInteractor {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Setup())

    val uiState: StateFlow<MoveThePieceUiState> = combine(
        _viewState,
        countdownTimer.remainingSeconds
    ) { viewState, remainingSeconds ->
        // Handle time expiry during playing
        if (viewState is ViewState.Playing && remainingSeconds <= 0) {
            finishGame(wasCaptured = false)
        }
        viewState.toUiState(remainingSeconds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MoveThePieceUiState.Setup()
    )

    private var currentHighScore: Int = 0

    init {
        viewModelScope.launch {
            userRepository.userUpdates()
                .map { it.highScores.moveThePiece }
                .collect { currentHighScore = it }
        }
        countdownTimer.set(MoveThePieceUiState.DEFAULT_DURATION_SECONDS)
    }

    override fun onTrainingModeToggled(enabled: Boolean) {
        val currentState = _viewState.value
        if (currentState is ViewState.Setup) {
            _viewState.value = currentState.copy(isTrainingMode = enabled)
        }
    }

    override fun onPieceSelected(piece: Piece) {
        val currentState = _viewState.value
        if (currentState is ViewState.Setup) {
            _viewState.value = currentState.copy(selectedPiece = piece)
        }
    }

    override fun onPlayClicked() {
        val currentState = _viewState.value
        if (currentState !is ViewState.Setup) return

        val initialPiece = if (currentState.isTrainingMode) {
            currentState.selectedPiece
        } else {
            Piece.Bishop // Start with Bishop in non-training mode
        }

        gameEngine.startGame(
            piece = initialPiece,
            requiredMoves = INITIAL_REQUIRED_MOVES,
            opposingPieceCount = INITIAL_OPPOSING_PIECES,
            isTrainingMode = currentState.isTrainingMode,
        )

        countdownTimer.start(scope = viewModelScope)

        val engineState = gameEngine.getState()
        _viewState.value = ViewState.Playing(
            boardState = MoveThePieceBoardState(
                playerPieceLocus = engineState.playerPieceLocus,
                playerPiece = engineState.playerPiece,
                opposingPieces = engineState.opposingPieces,
                visitedSquares = engineState.visitedSquares
            ),
            movesRemaining = engineState.movesRemaining,
            currentScore = engineState.currentScore,
            isTrainingMode = currentState.isTrainingMode
        )
    }

    override fun onSquareClicked(locus: Locus) {
        val currentState = _viewState.value
        if (currentState !is ViewState.Playing) return

        when (val result = gameEngine.makeMove(locus)) {
            is MoveResult.Invalid -> {
                // Do nothing - invalid move
            }
            is MoveResult.Captured -> {
                finishGame(wasCaptured = true)
            }
            is MoveResult.Success -> {
                _viewState.value = currentState.copy(
                    boardState = result.newState.toBoardState(),
                    movesRemaining = result.newState.movesRemaining,
                    currentScore = result.newState.currentScore
                )
            }
            is MoveResult.LevelComplete -> {
                _viewState.value = currentState.copy(
                    boardState = result.newState.toBoardState(),
                    movesRemaining = result.newState.movesRemaining,
                    currentScore = result.newState.currentScore
                )
            }
        }
    }

    override fun onPlayAgain() {
        gameEngine.reset()
        countdownTimer.set(MoveThePieceUiState.DEFAULT_DURATION_SECONDS)
        _viewState.value = ViewState.Setup()
    }

    private fun finishGame(wasCaptured: Boolean) {
        countdownTimer.stop()

        val playingState = _viewState.value as? ViewState.Playing ?: return
        val score = playingState.currentScore
        val isNewHighScore = score > currentHighScore && !playingState.isTrainingMode

        viewModelScope.launch {
            onComplete(
                MoveThePieceResult(
                    score = score,
                    timeSpentMillis = countdownTimer.elapsedMillis(),
                    isTrainingMode = playingState.isTrainingMode
                )
            )
        }

        _viewState.value = ViewState.GameOver(
            lastBoardState = playingState.boardState,
            finalScore = score,
            isNewHighScore = isNewHighScore,
            previousHighScore = currentHighScore,
            wasCaptured = wasCaptured
        )
    }

    private sealed class ViewState {
        abstract fun toUiState(remainingSeconds: Int): MoveThePieceUiState

        data class Setup(
            val isTrainingMode: Boolean = true,
            val selectedPiece: Piece = Piece.Rook
        ) : ViewState() {
            override fun toUiState(remainingSeconds: Int) = MoveThePieceUiState.Setup(
                isTrainingMode = isTrainingMode,
                selectedPiece = selectedPiece,
                timeRemainingSeconds = MoveThePieceUiState.DEFAULT_DURATION_SECONDS
            )
        }

        data class Playing(
            val boardState: MoveThePieceBoardState,
            val movesRemaining: Int,
            val currentScore: Int,
            val isTrainingMode: Boolean
        ) : ViewState() {
            override fun toUiState(remainingSeconds: Int) = MoveThePieceUiState.Playing(
                boardData = boardState.toBoardViewData(),
                playerPiece = boardState.playerPiece,
                playerPieceLocus = boardState.playerPieceLocus,
                movesRemaining = movesRemaining,
                currentScore = currentScore,
                timeRemainingSeconds = remainingSeconds,
                visitedSquares = boardState.visitedSquares,
                isTrainingMode = isTrainingMode
            )
        }

        data class GameOver(
            val lastBoardState: MoveThePieceBoardState,
            val finalScore: Int,
            val isNewHighScore: Boolean,
            val previousHighScore: Int,
            val wasCaptured: Boolean
        ) : ViewState() {
            override fun toUiState(remainingSeconds: Int) = MoveThePieceUiState.GameOver(
                boardData = lastBoardState.toBoardViewData(),
                finalScore = finalScore,
                isNewHighScore = isNewHighScore,
                previousHighScore = previousHighScore,
                wasCaptured = wasCaptured
            )
        }
    }

    companion object {
        private const val INITIAL_REQUIRED_MOVES = 1
        private const val INITIAL_OPPOSING_PIECES = 2
    }
}

private fun GameEngineState.toBoardState() = MoveThePieceBoardState(
    playerPieceLocus = playerPieceLocus,
    playerPiece = playerPiece,
    opposingPieces = opposingPieces,
    visitedSquares = visitedSquares
)
