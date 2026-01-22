package com.paulcraciunas.screens.puzzles.streak.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.GetStreakPuzzle
import com.paulcraciunas.domain.api.OnStreakComplete
import com.paulcraciunas.domain.api.OnStreakPuzzleComplete
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper.OnSquareClick
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PuzzleStreakViewModel @Inject constructor(
    private val getStreakPuzzle: GetStreakPuzzle,
    private val onStreakPuzzleComplete: OnStreakPuzzleComplete,
    private val onStreakComplete: OnStreakComplete,
    gameFactory: GameFactory,
) : ViewModel(), PuzzleStreakScreenInteractor {
    private val helper = PuzzleViewModelHelper(puzzleInteractor = gameFactory.puzzleInteractor())

    private val _uiState = MutableStateFlow<PuzzleStreakUiState>(PuzzleStreakUiState.Loading)
    val uiState: StateFlow<PuzzleStreakUiState> = _uiState.asStateFlow()

    private var currentStreakCount: Int = 0

    init {
        loadPuzzle()
    }

    private fun loadPuzzle() {
        viewModelScope.launch {
            try {
                val data = getStreakPuzzle()
                currentStreakCount = data.currentStreakCount
                _uiState.value = PuzzleStreakUiState.Playing(
                    data = helper.load(data.puzzle),
                    streakCount = currentStreakCount,
                    hintEnabled = true,
                    showAbandonDialog = false,
                    promotion = null,
                )
            } catch (_: Exception) {
                _uiState.value = PuzzleStreakUiState.Failed
            }
        }
    }

    override fun onSquareClicked(selection: Locus) = whilePlaying {
        handleMoveResult(helper.handleSquareClick(selection))
    }

    override fun onPromote(to: Piece) = whilePlaying { state->
        state.promotion?.let {
            handleMoveResult(helper.promote(to, state.promotion.at))
        }
    }

    override fun onHintRequested() {
        val state = _uiState.value
        if (state !is PuzzleStreakUiState.Playing || !state.hintEnabled) return

        helper.hint()
        _uiState.value = state.copy(
            data = helper.buildPuzzleData(),
            hintEnabled = false,
        )
    }

    override fun onAbandon() = whilePlaying { state ->
        _uiState.value = state.copy(showAbandonDialog = true)
    }

    override fun onAbandonConfirmed() = whilePlaying {
        helper.resign()
        viewModelScope.launch {
            val isNewHighScore = onStreakComplete(currentStreakCount)
            _uiState.value = PuzzleStreakUiState.StreakEnded(
                data = helper.buildPuzzleData(),
                finalStreakCount = currentStreakCount,
                isNewHighScore = isNewHighScore,
                showSummary = true,
            )
        }
    }

    override fun onAbandonDismissed() = whilePlaying { state ->
        _uiState.value = state.copy(showAbandonDialog = false)
    }

    override fun onNewStreak() {
        currentStreakCount = 0
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
        if (isSuccess) { // continue streak
            currentStreakCount++
            viewModelScope.launch {
                onStreakPuzzleComplete()
                loadNextPuzzle()
            }
        } else { // streak ends
            viewModelScope.launch {
                val isNewHighScore = onStreakComplete(currentStreakCount)
                _uiState.value = PuzzleStreakUiState.StreakEnded(
                    data = helper.buildPuzzleData(),
                    finalStreakCount = currentStreakCount,
                    isNewHighScore = isNewHighScore,
                    showSummary = true,
                )
            }
        }
    }

    private suspend fun loadNextPuzzle() {
        try {
            _uiState.value = PuzzleStreakUiState.Playing(
                data = helper.load(getStreakPuzzle().puzzle),
                streakCount = currentStreakCount,
                hintEnabled = true,
                showAbandonDialog = false,
                promotion = null,
            )
        } catch (_: Exception) {
            // If we can't load next puzzle, end the streak
            val isNewHighScore = onStreakComplete(currentStreakCount)
            _uiState.value = PuzzleStreakUiState.StreakEnded(
                data = helper.buildPuzzleData(),
                finalStreakCount = currentStreakCount,
                isNewHighScore = isNewHighScore,
                showSummary = true,
            )
        }
    }

    private fun whilePlaying(block: (PuzzleStreakUiState.Playing) -> Unit) {
        val state = _uiState.value
        if (state !is PuzzleStreakUiState.Playing) return
        block(state)
    }
}
