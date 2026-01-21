package com.paulcraciunas.screens.puzzles.rush.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.CountdownTimer
import com.paulcraciunas.domain.api.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.PuzzleRushResult
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewDataBuilder
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PuzzleRushViewModel @Inject constructor(
    private val puzzleSeries: GetBufferedPuzzleSeries,
    private val onPuzzleRushComplete: OnPuzzleRushComplete,
    private val countdownTimer: CountdownTimer,
    private val userRepository: UserRepository,
    gameFactory: GameFactory,
) : ViewModel(), PuzzleRushScreenInteractor {
    private val boardViewBuilder = BoardViewDataBuilder()
    private val puzzleInteractor = gameFactory.puzzleInteractor()

    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val uiState: StateFlow<PuzzleRushUiState> = combine(
        _gameState,
        countdownTimer.remainingSeconds
    ) { gameState, remainingSeconds ->
        // Handle time expiry during playing
        val state = if (gameState is GameState.Playing && remainingSeconds <= 0) {
            _gameState.value = finishRush(gameState)
            _gameState.value
        } else gameState
        state.toUiState(remainingSeconds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PuzzleRushUiState.Loading
    )

    private var currentHighScore: Int = 0

    init {
        loadPuzzles()
    }

    private fun loadPuzzles() {
        viewModelScope.launch {
            try {
                currentHighScore = userRepository.get().highScores.puzzleRush
                puzzleSeries()
                val puzzle = puzzleSeries.next()
                _gameState.value = if (puzzle == null) {
                    GameState.Failed
                } else {
                    countdownTimer.set(durationSeconds = DURATION_SECONDS)
                    loadPuzzleIntoInteractor(puzzle)
                    GameState.Ready(puzzleData = currentPuzzleData())
                }
            } catch (_: Exception) {
                _gameState.value = GameState.Failed
            }
        }
    }

    private fun loadPuzzleIntoInteractor(puzzle: Puzzle) {
        puzzleInteractor.load(puzzle)
        boardViewBuilder.load(puzzle)
    }

    override fun onSquareClicked(selection: Locus) {
        val state = _gameState.value
        if (state is GameState.Ready) { // Start the rush on first interaction
            startRush()
        } else if (state !is GameState.Playing) return

        if (boardViewBuilder.selected != null) {
            val current = boardViewBuilder.selected!!
            if (current == selection || !puzzleInteractor.canPlay(current, selection)) {
                boardViewBuilder.clearSelection()
                updatePlayingState(playingState)
            } else if (puzzleInteractor.canPromote(current, selection)) {
                _gameState.value = playingState.copy(
                    promotion = PuzzleRushUiState.Playing.Promotion(showChooser = true, at = selection)
                )
            } else {
                puzzleInteractor.play(current, selection)
                refreshBoardWithAnimation()
                handleMoveResult(playingState)
            }
        } else {
            boardViewBuilder.withSelection(selection, puzzleInteractor.moves(selection))
            updatePlayingState(playingState)
        }
    }

    override fun onPromote(to: Piece) {
        // make sure we don't crash if user waits to promote until rush ends
        if (_gameState.value !is GameState.Playing) return
        puzzleInteractor.promote(boardViewBuilder.selected!!, playingState.promotion!!.at, to)
        refreshBoardWithAnimation()
        handleMoveResult(playingState)
    }

    override fun onPlayAgain() {
        loadPuzzles()
    }

    override fun onDismissSummary() {
        val currentState = _gameState.value
        if (currentState is GameState.Finished) {
            _gameState.value = currentState.copy(showSummaryDialog = false)
        }
    }

    private fun startRush() {
        countdownTimer.start(scope = viewModelScope)
        _gameState.value = GameState.Playing(
            puzzleData = currentPuzzleData(),
            results = emptyList(),
            promotion = null,
        )
    }

    private fun handleMoveResult(currentState: GameState.Playing) {
        if (puzzleInteractor.isOver()) {
            val success = puzzleInteractor.isSuccess()
            val newResult = PuzzleResult(
                id = puzzleInteractor.id,
                rating = puzzleInteractor.rating,
                success = success
            )
            val updatedResults = currentState.results + newResult

            if (!success) {
                _gameState.value = finishRush(currentState.copy(results = updatedResults))
            } else {
                // Move to next puzzle
                viewModelScope.launch {
                    val nextPuzzle = puzzleSeries.next()
                    if (nextPuzzle != null && _gameState.value !is GameState.Finished) {
                        loadPuzzleIntoInteractor(nextPuzzle)
                        _gameState.value = currentState.copy(
                            puzzleData = currentPuzzleData(),
                            results = updatedResults,
                            promotion = null,
                        )
                    } else {
                        _gameState.value = finishRush(currentState.copy(results = updatedResults))
                    }
                }
            }
        } else {
            updatePlayingState(currentState)
        }
    }

    private fun finishRush(playingState: GameState.Playing): GameState {
        countdownTimer.stop()

        // Clear selection and animation for final board state
        val finalBoardData = boardViewBuilder.build()

        val results = playingState.results
        val puzzlesSolved = results.count { it.success }
        val isNewHighScore = puzzlesSolved > currentHighScore

        // Log the result
        viewModelScope.launch {
            onPuzzleRushComplete(
                PuzzleRushResult(
                    puzzlesSolved = puzzlesSolved,
                    puzzlesFailed = results.count { !it.success },
                    failedPuzzleIds = results.filter { !it.success }.mapNotNull { it.id },
                    timeSpentMillis = countdownTimer.elapsedMillis(),
                )
            )
        }
        return GameState.Finished(
            puzzleData = playingState.puzzleData.copy(boardData = finalBoardData),
            results = results,
            showSummaryDialog = true,
            isNewHighScore = isNewHighScore,
        )
    }

    private fun refreshBoardWithAnimation() {
        boardViewBuilder.refresh()
        puzzleInteractor.lastPly?.let { lastPly ->
            boardViewBuilder.withAnimatingPiece(lastPly.from, lastPly.to)
        }
    }

    private fun updatePlayingState(currentState: GameState.Playing) {
        _gameState.value = currentState.copy(
            puzzleData = currentPuzzleData(),
            promotion = null,
        )
    }

    private fun currentPuzzleData(): PuzzleRushUiState.PuzzleData =
        PuzzleRushUiState.PuzzleData(
            rating = puzzleInteractor.rating,
            player = puzzleInteractor.player,
            boardData = boardViewBuilder.build(),
        )
    private val playingState: GameState.Playing
        get() = _gameState.value as GameState.Playing

    // Internal state representation
    private sealed class GameState {
        abstract fun toUiState(remainingSeconds: Int): PuzzleRushUiState

        data object Loading : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Loading
        }

        data object Failed : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Failed
        }

        data class Ready(
            val puzzleData: PuzzleRushUiState.PuzzleData,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Ready(
                data = puzzleData,
                timeRemainingSeconds = DURATION_SECONDS,
            )
        }

        data class Playing(
            val puzzleData: PuzzleRushUiState.PuzzleData,
            val results: List<PuzzleResult>,
            val promotion: PuzzleRushUiState.Playing.Promotion?,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Playing(
                data = puzzleData,
                timeRemainingSeconds = remainingSeconds,
                results = results,
                promotion = promotion,
            )
        }

        data class Finished(
            val puzzleData: PuzzleRushUiState.PuzzleData,
            val results: List<PuzzleResult>,
            val showSummaryDialog: Boolean,
            val isNewHighScore: Boolean,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Finished(
                data = puzzleData,
                timeRemainingSeconds = remainingSeconds,
                results = results,
                showSummaryDialog = showSummaryDialog,
                isNewHighScore = isNewHighScore,
            )
        }
    }

    companion object {
        const val DURATION_SECONDS = 3 * 60 // 3 minutes
    }
}
