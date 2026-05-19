package com.paulcraciunas.screens.puzzles.rush.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.DefaultTimer
import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.domain.api.puzzles.PuzzleAnalysisData
import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleRushResult
import com.paulcraciunas.game.logic.api.PuzzleInteractor
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper.OnSquareClick
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PuzzleRushViewModel @Inject constructor(
    private val puzzleSeries: GetBufferedPuzzleSeries,
    private val onPuzzleRushComplete: OnPuzzleRushComplete,
    @param:DefaultTimer private val countdownTimer: CountdownTimer,
    private val userRepository: UserRepository,
    private val getPuzzleFen: GetPuzzleFen,
    puzzleInteractor: PuzzleInteractor,
) : ViewModel(), PuzzleRushScreenInteractor {
    private val helper = PuzzleViewModelHelper(puzzleInteractor = puzzleInteractor)

    private val _navigateToAnalysis = Channel<PuzzleAnalysisData>(Channel.BUFFERED)
    val navigateToAnalysis: Flow<PuzzleAnalysisData> = _navigateToAnalysis.receiveAsFlow()

    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val uiState: StateFlow<PuzzleRushUiState> = combine(
        _gameState,
        countdownTimer.remaining.map { it.seconds }
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
                    Timber.w("No puzzles available for puzzle rush")
                    GameState.Failed
                } else {
                    countdownTimer.set(durationSeconds = DURATION_SECONDS)
                    helper.load(puzzle)
                    GameState.Ready(puzzleData = helper.buildPuzzleData())
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load puzzle rush series")
                _gameState.value = GameState.Failed
            }
        }
    }

    override fun onSquareClicked(selection: Locus) {
        val state = _gameState.value
        if (state is GameState.Ready) { // Start the rush on first interaction
            startRush()
        } else if (state !is GameState.Playing) return

        handleMoveResult(helper.handleSquareClick(selection))
    }

    override fun onPromote(to: Piece) {
        val state = _gameState.value
        if (state !is GameState.Playing || state.promotion == null) return

        handleMoveResult(helper.promote(to, state.promotion.at))
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

    override fun onAnalyzeFailedPuzzle(puzzleId: Int) {
        viewModelScope.launch {
            getPuzzleFen(puzzleId)?.let { data -> _navigateToAnalysis.send(data) } ?: Timber.w("Failed to get puzzle fen")
        }
    }

    private fun startRush() {
        countdownTimer.start(scope = viewModelScope)
        _gameState.value = GameState.Playing(
            puzzleData = helper.buildPuzzleData(),
            results = emptyList(),
            promotion = null,
        )
    }

    private fun handleMoveResult(result: OnSquareClick) {
        val currentState = _gameState.value
        if (currentState !is GameState.Playing) return

        when {
            result.promotion != null -> _gameState.value = currentState.copy(promotion = result.promotion)
            !result.isOver -> {
                _gameState.value = currentState.copy(
                    puzzleData = helper.buildPuzzleData(),
                    promotion = null,
                )
            }
            else -> {
                handlePuzzleOver(currentState, result.isSuccess)
            }
        }
    }

    private fun handlePuzzleOver(currentState: GameState.Playing, isSuccess: Boolean) {
        val newResult = PuzzleResult(
            id = helper.id,
            rating = helper.rating,
            success = isSuccess
        )
        val updatedResults = currentState.results + newResult

        if (!isSuccess) {
            _gameState.value = finishRush(currentState.copy(results = updatedResults))
        } else {
            // Move to next puzzle
            viewModelScope.launch {
                val nextPuzzle = puzzleSeries.next()
                if (nextPuzzle != null && _gameState.value !is GameState.Finished) {
                    helper.load(nextPuzzle)
                    _gameState.value = currentState.copy(
                        puzzleData = helper.buildPuzzleData(),
                        results = updatedResults,
                        promotion = null,
                    )
                } else {
                    if (nextPuzzle == null) {
                        Timber.w("Puzzle buffer exhausted after %d puzzles", updatedResults.size)
                    }
                    _gameState.value = finishRush(currentState.copy(results = updatedResults))
                }
            }
        }
    }

    private fun finishRush(playingState: GameState.Playing): GameState {
        countdownTimer.stop()

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
            puzzleData = helper.buildPuzzleData(),
            results = results,
            showSummaryDialog = true,
            isNewHighScore = isNewHighScore,
        )
    }

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
            val puzzleData: PuzzleData,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Ready(
                data = puzzleData,
                timeRemainingSeconds = DURATION_SECONDS,
            )
        }

        data class Playing(
            val puzzleData: PuzzleData,
            val results: List<PuzzleResult>,
            val promotion: PuzzleViewModelHelper.Promotion?,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Playing(
                data = puzzleData,
                timeRemainingSeconds = remainingSeconds,
                results = results,
                promotion = promotion,
            )
        }

        data class Finished(
            val puzzleData: PuzzleData,
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
