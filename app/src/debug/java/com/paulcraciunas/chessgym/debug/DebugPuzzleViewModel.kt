package com.paulcraciunas.chessgym.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.logic.builders.Builders
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DebugPuzzleViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
) : ViewModel() {
    private val helper = PuzzleViewModelHelper(puzzleInteractor = Builders.puzzleInteractor())

    private val _uiState = MutableStateFlow<DebugPuzzleUiState>(DebugPuzzleUiState.Idle)
    val uiState: StateFlow<DebugPuzzleUiState> = _uiState.asStateFlow()

    fun loadPuzzle(id: Int) {
        _uiState.value = DebugPuzzleUiState.Loading
        viewModelScope.launch {
            try {
                val puzzle = puzzleRepository.getById(id)
                if (puzzle == null) {
                    _uiState.value = DebugPuzzleUiState.Error("Puzzle #$id not found")
                    return@launch
                }
                val data = helper.load(puzzle)
                _uiState.value = DebugPuzzleUiState.Playing(data = data, promotion = null)
            } catch (e: Exception) {
                Timber.w(e, "Failed to load debug puzzle #%d", id)
                _uiState.value = DebugPuzzleUiState.Error("Failed to load puzzle: ${e.message}")
            }
        }
    }

    fun onSquareClicked(selection: Locus) {
        val state = _uiState.value
        if (state !is DebugPuzzleUiState.Playing) return

        val result = helper.handleSquareClick(selection)
        when {
            result.promotion != null -> {
                _uiState.value = state.copy(promotion = result.promotion)
            }
            result.isOver -> {
                _uiState.value = DebugPuzzleUiState.Finished(
                    data = result.data,
                    isSuccess = result.isSuccess,
                )
            }
            else -> {
                _uiState.value = state.copy(data = result.data, promotion = null)
            }
        }
    }

    fun onPromote(to: Piece) {
        val state = _uiState.value
        if (state !is DebugPuzzleUiState.Playing || state.promotion == null) return

        val result = helper.promote(to, state.promotion.at)
        if (result.isOver) {
            _uiState.value = DebugPuzzleUiState.Finished(
                data = result.data,
                isSuccess = result.isSuccess,
            )
        } else {
            _uiState.value = state.copy(data = result.data, promotion = null)
        }
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
        val promotion: PuzzleViewModelHelper.Promotion?,
    ) : BoardState()

    data class Finished(
        override val data: PuzzleData,
        val isSuccess: Boolean,
    ) : BoardState()
}
