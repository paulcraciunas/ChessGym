package com.paulcraciunas.chessgym.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DebugPuzzleViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
) : ViewModel() {
    private val helper = PuzzleViewModelHelper()

    private val _uiState = MutableStateFlow<DebugPuzzleUiState>(DebugPuzzleUiState.Idle)
    val uiState: StateFlow<DebugPuzzleUiState> = _uiState.asStateFlow()

    fun loadPuzzle(id: Int) {
        _uiState.value = DebugPuzzleUiState.Loading
        viewModelScope.launch {
            try {
                val puzzle = puzzleRepository.getById(id)
                _uiState.update {
                    if (puzzle == null) DebugPuzzleUiState.Error("Puzzle #$id not found")
                    else DebugPuzzleUiState.Playing(data = helper.load(puzzle), promotion = null)
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load debug puzzle #%d", id)
                _uiState.value = DebugPuzzleUiState.Error("Failed to load puzzle: ${e.message}")
            }
        }
    }

    fun onSquareClicked(selection: Locus) {
        _uiState.updatePlaying {
            val result = helper.handleSquareClick(selection)
            if (!result.isOver) it.copy(data = result.data, promotion = result.promotion)
            else DebugPuzzleUiState.Finished(data = result.data, isSuccess = result.isSuccess)
        }
    }

    fun onPromote(to: Piece) {
        _uiState.updatePlaying {
            if (it.promotion == null) it
            else {
                val result = helper.promote(to, it.promotion.at)
                if (!result.isOver) it.copy(data = result.data, promotion = null)
                else DebugPuzzleUiState.Finished(data = result.data, isSuccess = result.isSuccess)
            }
        }
    }

    private inline fun MutableStateFlow<DebugPuzzleUiState>.updatePlaying(
        crossinline function: (DebugPuzzleUiState.Playing) -> DebugPuzzleUiState,
    ) = update { state ->
        if (state !is DebugPuzzleUiState.Playing) state else function(state)
    }
}

sealed class DebugPuzzleUiState {
    data object Idle : DebugPuzzleUiState()
    data object Loading : DebugPuzzleUiState()
    data class Error(val message: String) : DebugPuzzleUiState()

    abstract class BoardState : DebugPuzzleUiState() {
        abstract val data: PuzzleData
    }

    data class Playing(
        override val data: PuzzleData,
        val promotion: PuzzleViewModelHelper.Promotion2?,
    ) : BoardState()

    data class Finished(
        override val data: PuzzleData,
        val isSuccess: Boolean,
    ) : BoardState()
}
