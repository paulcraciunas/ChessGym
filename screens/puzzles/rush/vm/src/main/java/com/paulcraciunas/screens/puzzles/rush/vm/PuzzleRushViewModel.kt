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
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.PuzzleResult
import com.paulcraciunas.screens.data.PuzzleSessionFactory
import com.paulcraciunas.screens.data.SessionSettings
import com.paulcraciunas.screens.data.runAs
import com.paulcraciunas.screens.data.updateAs
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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
    appSettingsRepository: AppSettingsRepository,
    private val userRepository: UserRepository,
    private val getPuzzleFen: GetPuzzleFen,
) : ViewModel() {
    private val factory = PuzzleSessionFactory(withSolution = false)
    private val session = factory.get()
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
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PuzzleRushUiState.Loading
    )

    init {
        session.bindSettings(viewModelScope, appSettingsRepository.appSettings.map {
            SessionSettings(it.autoPromote, it.enableAnimations)
        })
        observeBoardState()
        loadPuzzles()
    }

    private fun observeBoardState() {
        viewModelScope.launch {
            session.data.collect { boardState ->
                _gameState.updateAs { it: GameState.Ready -> it.copy(puzzleData = boardState) }
                _gameState.updateAs { it: GameState.Playing -> it.copy(puzzleData = boardState) }
                _gameState.runAs<GameState.Playing> {
                    if (boardState.isOver) {
                        recordResultIfNeeded(boardState)
                        if (boardState.interactive) onPuzzleCompleted(boardState)
                    }
                }
            }
        }
    }

    private fun observeTimer() {
        timerObserverJob?.cancel()
        timerObserverJob = viewModelScope.launch {
            countdownTimer.remaining.first { it.roundSeconds() <= 0 }
            _gameState.runAs<GameState.Playing> { finishRush(it.results) }
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
                countdownTimer.set(durationSeconds = TOTAL_RUSH_DURATION_SECONDS)
                factory.load(viewModelScope, puzzle)
                _gameState.update { GameState.Ready(puzzleData = session.currentState) }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load puzzle rush series")
                _gameState.update { GameState.Failed }
            }
        }
    }

    fun onSquareClicked(selection: Locus) {
        _gameState.runAs<GameState.Ready> { startRush() }
        _gameState.runAs<GameState.Playing> { session.onClick(selection) }
    }

    fun onPromote(to: Piece) = session.promoteIfPending(to)
    fun onPlayAgain() = loadPuzzles()
    fun onDismissSummary() = _gameState.updateAs { it: GameState.Finished -> it.copy(showSummaryDialog = false) }
    fun onAnalyzeFailedPuzzle(puzzleId: Int) = viewModelScope.launch {
        getPuzzleFen(puzzleId)?.let { data -> _navigateToAnalysis.send(data) } ?: Timber.w("Failed to get puzzle fen")
    }

    private fun startRush() {
        countdownTimer.start(scope = viewModelScope)
        observeTimer()
        _gameState.update { GameState.Playing(puzzleData = session.currentState, results = emptyList()) }
    }

    private fun recordResultIfNeeded(boardState: BoardState) = _gameState.updateAs { state: GameState.Playing ->
        if (state.results.any { it.id == boardState.id }) state
        else state.copy(
            results = state.results + PuzzleResult(id = boardState.id, rating = boardState.rating!!, success = boardState.won)
        )
    }

    private fun onPuzzleCompleted(boardState: BoardState): Unit = _gameState.runAs<GameState.Playing> { state ->
        if (boardState.won) {
            viewModelScope.launch {
                try {
                    val nextPuzzle = puzzleSeries.next()
                    factory.load(viewModelScope, nextPuzzle)
                    _gameState.updateAs { it: GameState.Playing -> it.copy(puzzleData = session.currentState) }
                } catch (e: Exception) {
                    Timber.w(e, "Puzzle buffer exhausted after %d puzzles", state.results.size)
                    finishRush(state.results)
                }
            }
        } else {
            finishRush(state.results)
        }
    }

    private fun finishRush(results: List<PuzzleResult>) {
        countdownTimer.stop()
        val elapsedMillis = countdownTimer.elapsedMillis()

        _gameState.runAs<GameState.Playing> { state ->
            val puzzlesSolved = results.count { it.success }
            val rushResult = PuzzleRushResult(
                puzzlesSolved = puzzlesSolved,
                puzzlesFailed = results.count { !it.success },
                failedPuzzleIds = results.filter { !it.success }.mapNotNull { it.id },
                timeSpentMillis = elapsedMillis,
            )
            viewModelScope.launch { onPuzzleRushComplete(rushResult) }

            _gameState.update {
                GameState.Finished(
                    puzzleData = state.puzzleData,
                    results = results,
                    showSummaryDialog = true,
                    isNewHighScore = puzzlesSolved > currentHighScore,
                )
            }
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

        data class Ready(val puzzleData: BoardState) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Ready(
                data = puzzleData,
                timeRemainingSeconds = TOTAL_RUSH_DURATION_SECONDS,
            )
        }

        data class Playing(val puzzleData: BoardState, val results: List<PuzzleResult>) : GameState() {
            override fun toUiState(remainingSeconds: Int) = PuzzleRushUiState.Playing(
                data = puzzleData,
                timeRemainingSeconds = remainingSeconds.coerceAtLeast(0),
                results = results,
            )
        }

        data class Finished(
            val puzzleData: BoardState,
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
        const val TOTAL_RUSH_DURATION_SECONDS = 3 * 60
    }
}
