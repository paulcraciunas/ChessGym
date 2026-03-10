package com.paulcraciunas.screens.puzzles.streak.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakPuzzleComplete
import com.paulcraciunas.game.logic.api.PuzzleInteractor
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper.OnSquareClick
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PuzzleStreakViewModel @Inject constructor(
    private val getStreakPuzzle: GetStreakPuzzle,
    private val onStreakPuzzleComplete: OnStreakPuzzleComplete,
    private val onStreakComplete: OnStreakComplete,
    private val timer: Timer,
    puzzleInteractor: PuzzleInteractor,
) : ViewModel(), PuzzleStreakScreenInteractor {
    private val helper = PuzzleViewModelHelper(puzzleInteractor = puzzleInteractor)

    private val _uiState = MutableStateFlow<PuzzleStreakUiState>(PuzzleStreakUiState.Loading)
    val uiState: StateFlow<PuzzleStreakUiState> = _uiState.asStateFlow()

    init {
        loadPuzzle()
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

    override fun onSquareClicked(selection: Locus) = whilePlayingInteractive {
        handleMoveResult(helper.handleSquareClick(selection))
    }

    override fun onPromote(to: Piece) = whilePlayingInteractive { state ->
        state.promotion?.let {
            handleMoveResult(helper.promote(to, state.promotion.at))
        }
    }

    override fun onHintRequested() = whilePlayingInteractive { state ->
        if (!state.hintEnabled) return@whilePlayingInteractive

        helper.hint()
        _uiState.value = state.copy(
            data = helper.buildPuzzleData(),
            hintEnabled = false,
        )
    }

    override fun onAbandon() = whilePlayingInteractive { state ->
        _uiState.value = state.copy(showAbandonDialog = true)
    }

    override fun onAbandonConfirmed() = whilePlaying { state ->
        // Start showing solution animation
        _uiState.value = state.copy(
            showAbandonDialog = false,
            isShowingSolution = true,
        )

        viewModelScope.launch {
            helper.playSolution { data ->
                val currentState = _uiState.value
                val canUpdate = currentState is PuzzleStreakUiState.Playing && currentState.isShowingSolution
                canUpdate.also { if (canUpdate) _uiState.value = currentState.copy(data = data) }
            }

            // After solution shown, end the streak
            endStreak()
        }
    }

    override fun onAbandonDismissed() = whilePlaying { state ->
        _uiState.value = state.copy(showAbandonDialog = false)
    }

    override fun onNewStreak() {
        _uiState.value = PuzzleStreakUiState.Loading
        loadPuzzle()
    }

    override fun onDismissSummary() {
        val state = _uiState.value
        if (state is PuzzleStreakUiState.StreakEnded) {
            _uiState.value = state.copy(showSummary = false)
        }
    }

    private fun handleMoveResult(result: OnSquareClick) = whilePlaying { state ->
        when {
            result.promotion != null -> _uiState.value = state.copy(promotion = result.promotion)
            !result.isOver -> _uiState.value = state.copy(data = helper.buildPuzzleData(), promotion = null)
            else -> handlePuzzleOver(result.isSuccess)
        }
    }

    private fun handlePuzzleOver(isSuccess: Boolean) {
        viewModelScope.launch {
            if (isSuccess) { // continue streak
                val timeSpent = timer.elapsed()
                onStreakPuzzleComplete(timeSpent)
                try {
                    loadNextPuzzle()
                } catch (e: Exception) {
                    Timber.w(e, "Failed to load next streak puzzle, ending streak")
                    endStreak()
                }
            } else { // streak ends
                endStreak()
            }
        }
    }

    private suspend fun loadNextPuzzle() {
        timer.start()
        val data = getStreakPuzzle()
        _uiState.value = PuzzleStreakUiState.Playing(
            data = helper.load(data.puzzle),
            streakCount = data.currentStreakCount,
            hintEnabled = true,
            showAbandonDialog = false,
            promotion = null,
        )
    }

    private suspend fun endStreak() {
        val timeSpent = timer.elapsed()
        val result = onStreakComplete(timeSpent)
        whilePlaying { _ ->
            _uiState.value = PuzzleStreakUiState.StreakEnded(
                data = helper.buildPuzzleData(),
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

    /** Same as [whilePlaying] but also blocks interaction while showing solution */
    private fun whilePlayingInteractive(block: (PuzzleStreakUiState.Playing) -> Unit) {
        val state = _uiState.value
        if (state !is PuzzleStreakUiState.Playing || state.isShowingSolution) return
        block(state)
    }
}
