package com.paulcraciunas.screens.puzzles.streak.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakPuzzleComplete
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.board.PIECE_MOVE_ANIMATION_DURATION_MS
import com.paulcraciunas.screens.data.BoardInteractionHelper
import com.paulcraciunas.screens.data.ClickResult
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.screens.data.PuzzleSolution
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PuzzleStreakViewModel @Inject constructor(
    private val getStreakPuzzle: GetStreakPuzzle,
    private val onStreakPuzzleComplete: OnStreakPuzzleComplete,
    private val onStreakComplete: OnStreakComplete,
    private val appSettingsRepository: AppSettingsRepository,
    private val timer: Timer,
) : ViewModel() {
    private val helper = BoardInteractionHelper(solution = PuzzleSolution())
    private var enableAnimations: Boolean = true

    private val _uiState = MutableStateFlow<PuzzleStreakUiState>(PuzzleStreakUiState.Loading)
    val uiState: StateFlow<PuzzleStreakUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        loadPuzzle()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { settings ->
                helper.autoPromote = settings.autoPromote
                enableAnimations = settings.enableAnimations
            }
        }
    }

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    private fun loadPuzzle() {
        viewModelScope.launch {
            try {
                loadNextPuzzle()
            } catch (e: Exception) {
                Timber.w(e, "Failed to load streak puzzle")
                _uiState.value = PuzzleStreakUiState.Failed
            }
        }
    }

    fun onSquareClicked(selection: Locus) = whilePlayingInteractive {
        handleMoveResult(helper.handleSquareClick(selection))
    }

    fun onPromote(to: Piece) = whilePlayingInteractive { state ->
        state.promotion?.let {
            handleMoveResult(helper.promote(to, state.promotion.at))
        }
    }

    fun onHintRequested() = _uiState.updateInteractive { state ->
        if (!state.hintEnabled) state
        else state.copy(
            data = helper.hint(),
            hintEnabled = false,
        )
    }

    fun onAbandon() = _uiState.updateInteractive { it.copy(showAbandonDialog = true) }
    fun onAbandonConfirmed() = whilePlaying {
        // Start showing solution animation
        _uiState.updateInteractive { it.copy(showAbandonDialog = false, isShowingSolution = true) }

        viewModelScope.launch {
            helper.playSolution { data ->
                _uiState.updatePlaying { it.copy(data = data) }
                return@playSolution _uiState.value is PuzzleStreakUiState.Playing
            }
            // After solution shown, end the streak
            endStreak()
        }
    }

    fun onAbandonDismissed() = _uiState.updatePlaying { it.copy(showAbandonDialog = false) }

    fun onNewStreak() {
        _uiState.update { PuzzleStreakUiState.Loading }
        loadPuzzle()
    }

    fun onDismissSummary() {
        _uiState.update { state ->
            if (state is PuzzleStreakUiState.StreakEnded) state.copy(showSummary = false)
            else state
        }
    }

    private fun handleMoveResult(result: ClickResult) {
        _uiState.updatePlaying { it.copy(data = result.data, promotion = result.promotion, isAnimating = result.data.isOver) }
        if (result.data.isOver) {
            onPuzzleCompleted(result)
        }
    }

    fun onNextPuzzle() = whilePlaying { state ->
        if (!state.isAwaitingNextPuzzle) return@whilePlaying

        viewModelScope.launch {
            goToNext()
        }
    }

    private fun onPuzzleCompleted(result: ClickResult) = whilePlaying {
        viewModelScope.launch {
            if (enableAnimations) {
                delay(ANIMATION_WAIT_MS)
            }

            if (result.data.won) {
                val timeSpent = timer.elapsed()
                onStreakPuzzleComplete(timeSpent)

                if (appSettingsRepository.appSettings.first().autoNextPuzzle) goToNext()
                else _uiState.updatePlaying { it.copy(isAwaitingNextPuzzle = true, isAnimating = false) }
            } else {
                endStreak()
            }
        }
    }

    private suspend fun goToNext() {
        try {
            loadNextPuzzle()
        } catch (e: Exception) {
            Timber.w(e, "Failed to load next streak puzzle, ending streak")
            endStreak()
        }
    }

    private suspend fun loadNextPuzzle() {
        timer.start()
        val data = getStreakPuzzle()
        _uiState.update {
            PuzzleStreakUiState.Playing(
                data = helper.load(PuzzlePlayableBoard(data.puzzle)),
                streakCount = data.currentStreakCount,
                isAwaitingNextPuzzle = false,
                hintEnabled = true,
                showAbandonDialog = false,
                promotion = null,
            )
        }
    }

    private suspend fun endStreak() {
        val timeSpent = timer.elapsed()
        val result = onStreakComplete(timeSpent)
        _uiState.updatePlaying {
            PuzzleStreakUiState.StreakEnded(
                data = helper.current(),
                finalStreakCount = result.finalStreakCount,
                isNewHighScore = result.isNewHighScore,
                showSummary = true,
            )
        }
    }

    private fun whilePlaying(block: (PuzzleStreakUiState.Playing) -> Unit) {
        val state = _uiState.value
        if (state !is PuzzleStreakUiState.Playing) return
        block(state)
    }

    /** Same as [whilePlaying] but also blocks interaction while showing solution, awaiting next, or animating */
    private fun whilePlayingInteractive(block: (PuzzleStreakUiState.Playing) -> Unit) {
        val state = _uiState.value
        if (state !is PuzzleStreakUiState.Playing || state.isShowingSolution || state.isAwaitingNextPuzzle || state.isAnimating) return
        block(state)
    }

    private inline fun MutableStateFlow<PuzzleStreakUiState>.updatePlaying(
        crossinline function: (PuzzleStreakUiState.Playing) -> PuzzleStreakUiState,
    ) = update { state ->
        if (state !is PuzzleStreakUiState.Playing) state else function(state)
    }

    private inline fun MutableStateFlow<PuzzleStreakUiState>.updateInteractive(
        crossinline function: (PuzzleStreakUiState.Playing) -> PuzzleStreakUiState,
    ) = update { state ->
        if (state !is PuzzleStreakUiState.Playing || state.isShowingSolution || state.isAwaitingNextPuzzle || state.isAnimating) state
        else function(state)
    }

    companion object {
        internal const val ANIMATION_WAIT_MS = PIECE_MOVE_ANIMATION_DURATION_MS + 50L
    }
}
