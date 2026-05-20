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
import com.paulcraciunas.game.logic.api.PuzzleInteractor
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.board.PIECE_MOVE_ANIMATION_DURATION_MS
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper.OnSquareClick
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
    puzzleInteractor: PuzzleInteractor,
) : ViewModel(), PuzzleRushScreenInteractor {
    private val helper = PuzzleViewModelHelper(puzzleInteractor = puzzleInteractor)
    private var enableAnimations: Boolean = true

    private val _navigateToAnalysis = Channel<PuzzleAnalysisData>(Channel.BUFFERED)
    val navigateToAnalysis: Flow<PuzzleAnalysisData> = _navigateToAnalysis.receiveAsFlow()

    private var currentHighScore: Int = 0
    private var timerObserverJob: Job? = null
    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val uiState: StateFlow<PuzzleRushUiState> = combine(
        _gameState,
        countdownTimer.remaining.map { it.seconds }
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
        _gameState.value = GameState.Loading
        countdownTimer.stop()
        viewModelScope.launch {
            try {
                puzzleSeries.start(this@launch)
                currentHighScore = userRepository.get().highScores.puzzleRush
                val puzzle = puzzleSeries.next()
                countdownTimer.set(durationSeconds = DURATION_SECONDS)
                val puzzleData = helper.load(puzzle)
                _gameState.value = GameState.Ready(puzzleData = puzzleData)
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
        } else if (state !is GameState.Playing || state.isAnimating) return

        handleMoveResult(helper.handleSquareClick(selection))
    }

    override fun onPromote(to: Piece) {
        val state = _gameState.value
        if (state !is GameState.Playing || state.promotion == null) return

        handleMoveResult(helper.promote(to, state.promotion.at))
    }

    override fun onPlayAgain() = loadPuzzles()

    override fun onDismissSummary() {
        _gameState.update { state ->
            if (state is GameState.Finished) state.copy(showSummaryDialog = false) else state
        }
    }

    override fun onAnalyzeFailedPuzzle(puzzleId: Int) {
        viewModelScope.launch {
            getPuzzleFen(puzzleId)?.let { data -> _navigateToAnalysis.send(data) }
                ?: Timber.w("Failed to get puzzle fen")
        }
    }

    private fun startRush() {
        countdownTimer.start(scope = viewModelScope)
        observeTimer()
        _gameState.value = GameState.Playing(
            puzzleData = helper.buildPuzzleData(),
            results = emptyList(),
            promotion = null,
            isAnimating = false,
        )
    }

    private fun handleMoveResult(result: OnSquareClick) {
        if (result.isOver) {
            onPuzzleCompleted(result)
            return
        }
        _gameState.update { state ->
            if (state !is GameState.Playing) return@update state
            when {
                result.promotion != null -> state.copy(promotion = result.promotion)
                else -> state.copy(puzzleData = result.data, promotion = null)
            }
        }
    }

    private fun onPuzzleCompleted(result: OnSquareClick) {
        _gameState.update { state ->
            if (state !is GameState.Playing) return@update state
            state.copy(
                puzzleData = result.data,
                promotion = null,
                isAnimating = true,
                results = state.results + PuzzleResult(
                    id = helper.id,
                    rating = helper.rating,
                    success = result.isSuccess,
                ),
            )
        }

        viewModelScope.launch {
            if (enableAnimations) {
                delay(ANIMATION_WAIT_MS)
            }

            val currentState = _gameState.value as? GameState.Playing ?: return@launch

            if (!result.isSuccess) {
                finishRush(currentState.results)
                return@launch
            }
            try {
                val nextData = helper.load(puzzleSeries.next())
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

        data class Ready(val puzzleData: PuzzleData) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Ready(
                data = puzzleData,
                timeRemainingSeconds = DURATION_SECONDS,
            )
        }

        data class Playing(
            val puzzleData: PuzzleData,
            val results: List<PuzzleResult>,
            val promotion: PuzzleViewModelHelper.Promotion?,
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
            val puzzleData: PuzzleData,
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
