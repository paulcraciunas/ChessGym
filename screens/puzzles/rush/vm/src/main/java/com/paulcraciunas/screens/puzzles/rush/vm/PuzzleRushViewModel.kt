package com.paulcraciunas.screens.puzzles.rush.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.DefaultTimer
import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleAnalysisData
import com.paulcraciunas.domain.api.puzzles.PuzzleRushResult
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.board.PIECE_MOVE_ANIMATION_DURATION_MS
import com.paulcraciunas.screens.common.model.BoardInteractionHelper
import com.paulcraciunas.screens.common.model.ClickResult
import com.paulcraciunas.screens.common.model.PlayableData
import com.paulcraciunas.screens.common.model.Promotion
import com.paulcraciunas.screens.common.model.PuzzlePlayableBoard
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PuzzleRushViewModel @Inject constructor(
    private val puzzleSeries: GetBufferedPuzzleSeries,
    private val onPuzzleRushComplete: OnPuzzleRushComplete,
    @param:DefaultTimer private val countdownTimer: CountdownTimer,
    private val appSettingsRepository: AppSettingsRepository,
    private val userRepository: UserRepository,
    private val getPuzzleFen: GetPuzzleFen,
) : ViewModel() {
    private val helper = BoardInteractionHelper()
    private var enableAnimations: Boolean = true

    private val _navigateToAnalysis = Channel<PuzzleAnalysisData>(Channel.BUFFERED)
    val navigateToAnalysis: Flow<PuzzleAnalysisData> = _navigateToAnalysis.receiveAsFlow()

    private var currentHighScore: Int = 0
    private var timerObserverJob: Job? = null
    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val uiState: StateFlow<PuzzleRushUiState> = combine(
        _gameState,
        countdownTimer.remaining.map { it.roundSeconds() }
    ) { gameState, remainingSeconds ->
        gameState.toUiState(remainingSeconds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PuzzleRushUiState.Loading
    )

    init {
        observeSettings()
        loadPuzzles()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { settings ->
                helper.autoPromote = settings.autoPromote
                enableAnimations = settings.enableAnimations
            }
        }
    }

    private fun observeTimer() {
        timerObserverJob?.cancel()
        timerObserverJob = viewModelScope.launch {
            countdownTimer.remaining.first { it.seconds <= 0 }
            val state = _gameState.value as? GameState.Playing ?: return@launch
            finishRush(state.results)
        }
    }

    private fun loadPuzzles() {
        _gameState.update { GameState.Loading }
        countdownTimer.stop()
        viewModelScope.launch {
            try {
                puzzleSeries.start(this@launch)
                currentHighScore = userRepository.get().highScores.puzzleRush
                val puzzle = puzzleSeries.next()
                countdownTimer.set(durationSeconds = DURATION_SECONDS)
                _gameState.update { GameState.Ready(puzzleData = helper.load(PuzzlePlayableBoard(puzzle))) }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load puzzle rush series")
                _gameState.update { GameState.Failed }
            }
        }
    }

    fun onSquareClicked(selection: Locus) {
        val state = _gameState.value
        if (state is GameState.Ready) { // Start the rush on first interaction
            startRush()
        } else if (state !is GameState.Playing || state.isAnimating) return

        handleMoveResult(helper.handleSquareClick(selection))
    }

    fun onPromote(to: Piece) {
        val state = _gameState.value
        if (state !is GameState.Playing || state.promotion == null) return

        handleMoveResult(helper.promote(to, state.promotion.at))
    }

    fun onPlayAgain() = loadPuzzles()

    fun onDismissSummary() {
        _gameState.update { state ->
            if (state is GameState.Finished) state.copy(showSummaryDialog = false) else state
        }
    }

    fun onAnalyzeFailedPuzzle(puzzleId: Int) {
        viewModelScope.launch {
            getPuzzleFen(puzzleId)?.let { data -> _navigateToAnalysis.send(data) }
                ?: Timber.w("Failed to get puzzle fen")
        }
    }

    private fun startRush() {
        countdownTimer.start(scope = viewModelScope)
        observeTimer()
        _gameState.update {
            GameState.Playing(
                puzzleData = helper.current(),
                results = emptyList(),
                promotion = null,
                isAnimating = false,
            )
        }
    }

    private fun handleMoveResult(result: ClickResult) {
        if (result.data.isOver) {
            onPuzzleCompleted(result)
        } else _gameState.update { state ->
            if (state !is GameState.Playing) state
            else state.copy(puzzleData = result.data, promotion = result.promotion)
        }
    }

    private fun onPuzzleCompleted(result: ClickResult) {
        _gameState.update { state ->
            if (state !is GameState.Playing) state
            else state.copy(
                puzzleData = result.data,
                promotion = null,
                isAnimating = true,
                results = state.results + PuzzleResult(
                    id = result.data.id,
                    rating = result.data.rating!!,
                    success = result.data.won,
                ),
            )
        }

        viewModelScope.launch {
            if (enableAnimations) {
                delay(ANIMATION_WAIT_MS)
            }

            val currentState = _gameState.value as? GameState.Playing ?: return@launch

            if (!result.data.won) {
                finishRush(currentState.results)
                return@launch
            }
            try {
                val nextData = helper.load(PuzzlePlayableBoard(puzzleSeries.next()))
                _gameState.update { state ->
                    if (state is GameState.Playing) {
                        state.copy(puzzleData = nextData, isAnimating = false)
                    } else state
                }
            } catch (e: Exception) {
                Timber.w(e, "Puzzle buffer exhausted after %d puzzles", currentState.results.size)
                finishRush(currentState.results)
            }
        }
    }

    private fun finishRush(results: List<PuzzleResult>) {
        countdownTimer.stop()
        val elapsedMillis = countdownTimer.elapsedMillis()

        _gameState.update { state ->
            if (state !is GameState.Playing) return@update state

            val puzzlesSolved = results.count { it.success }

            val rushResult = PuzzleRushResult(
                puzzlesSolved = puzzlesSolved,
                puzzlesFailed = results.count { !it.success },
                failedPuzzleIds = results.filter { !it.success }.mapNotNull { it.id },
                timeSpentMillis = elapsedMillis,
            )
            viewModelScope.launch { onPuzzleRushComplete(rushResult) }

            GameState.Finished(
                puzzleData = state.puzzleData,
                results = results,
                showSummaryDialog = true,
                isNewHighScore = puzzlesSolved > currentHighScore,
            )
        }
    }

    private sealed class GameState {
        abstract fun toUiState(remainingSeconds: Int): PuzzleRushUiState

        data object Loading : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Loading
        }

        data object Failed : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Failed
        }

        data class Ready(val puzzleData: PlayableData) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Ready(
                data = puzzleData,
                timeRemainingSeconds = DURATION_SECONDS,
            )
        }

        data class Playing(
            val puzzleData: PlayableData,
            val results: List<PuzzleResult>,
            val promotion: Promotion?,
            val isAnimating: Boolean,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Playing(
                data = puzzleData,
                timeRemainingSeconds = remainingSeconds.coerceAtLeast(0),
                results = if (isAnimating) results.dropLast(1) else results,
                promotion = promotion,
            )
        }

        data class Finished(
            val puzzleData: PlayableData,
            val results: List<PuzzleResult>,
            val showSummaryDialog: Boolean,
            val isNewHighScore: Boolean,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Finished(
                data = puzzleData,
                timeRemainingSeconds = 0,
                results = results,
                showSummaryDialog = showSummaryDialog,
                isNewHighScore = isNewHighScore,
            )
        }
    }

    companion object {
        const val DURATION_SECONDS = 3 * 60
        internal const val ANIMATION_WAIT_MS = PIECE_MOVE_ANIMATION_DURATION_MS + 50L
    }
}
