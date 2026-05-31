package com.paulcraciunas.chessgym.debug

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.PuzzleSessionFactory
import com.paulcraciunas.screens.data.runAs
import com.paulcraciunas.screens.data.updateAs
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
    private val factory = PuzzleSessionFactory(withSolution = false)
    private val session = factory.get()

    private val _uiState = MutableStateFlow<DebugPuzzleUiState>(DebugPuzzleUiState.Idle)
    val uiState: StateFlow<DebugPuzzleUiState> = _uiState.asStateFlow()

    init {
        observeBoardState()
    }

    private fun observeBoardState() {
        viewModelScope.launch {
            session.data.collect { boardState ->
                _uiState.updateAs { it: DebugPuzzleUiState.Playing -> it.copy(data = boardState) }
                _uiState.runAs<DebugPuzzleUiState.Playing> {
                    if (boardState.isOver) {
                        _uiState.update { DebugPuzzleUiState.Finished(data = boardState, isSuccess = boardState.won) }
                    }
                }
            }
        }
    }

    fun loadPuzzle(id: Int) {
        _uiState.update { DebugPuzzleUiState.Loading }
        viewModelScope.launch {
            try {
                val puzzle = puzzleRepository.getById(id)
                if (puzzle == null) {
                    _uiState.update { DebugPuzzleUiState.Error("Puzzle #$id not found") }
                } else {
                    factory.load(viewModelScope, puzzle)
                    _uiState.update { DebugPuzzleUiState.Playing(data = session.currentState) }
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load debug puzzle #%d", id)
                _uiState.update { DebugPuzzleUiState.Error("Failed to load puzzle: ${e.message}") }
            }
        }
    }

    fun onSquareClicked(selection: Locus) = _uiState.runAs<DebugPuzzleUiState.Playing> { session.onClick(selection) }
    fun onPromote(to: Piece) = session.promoteIfPending(to)
}

sealed class DebugPuzzleUiState {
    data object Idle : DebugPuzzleUiState()
    data object Loading : DebugPuzzleUiState()
    data class Error(val message: String) : DebugPuzzleUiState()

    @Immutable
    abstract class WithBoard : DebugPuzzleUiState() {
        abstract val data: BoardState
    }

    @Immutable
    data class Playing(override val data: BoardState) : WithBoard()

    @Immutable
    data class Finished(override val data: BoardState, val isSuccess: Boolean) : WithBoard()
}
