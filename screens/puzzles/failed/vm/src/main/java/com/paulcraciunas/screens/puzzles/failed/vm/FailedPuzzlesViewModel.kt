package com.paulcraciunas.screens.puzzles.failed.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.GetFailedPuzzles
import com.paulcraciunas.domain.api.OnFailedPuzzleComplete
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewDataBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FailedPuzzlesViewModel @Inject constructor(
    private val getFailedPuzzles: GetFailedPuzzles,
    private val onFailedPuzzleComplete: OnFailedPuzzleComplete,
    gameFactory: GameFactory,
) : ViewModel(), FailedPuzzlesScreenInteractor {
    private val boardViewBuilder = BoardViewDataBuilder()
    private val puzzleInteractor = gameFactory.puzzleInteractor()

    private val _uiState = MutableStateFlow<FailedPuzzlesUiState>(FailedPuzzlesUiState.Loading)
    val uiState: StateFlow<FailedPuzzlesUiState> = _uiState.asStateFlow()

    private var solvedCount: Int = 0

    init {
        loadPuzzles()
    }

    private fun loadPuzzles() {
        viewModelScope.launch {
            try {
                getFailedPuzzles.load()

                if (getFailedPuzzles.totalCount() == 0) {
                    _uiState.value = FailedPuzzlesUiState.Empty
                    return@launch
                }

                val puzzle = getFailedPuzzles.next()
                if (puzzle == null) {
                    _uiState.value = FailedPuzzlesUiState.Empty
                } else {
                    loadPuzzleIntoInteractor(puzzle)
                    _uiState.value = FailedPuzzlesUiState.Playing(
                        data = currentPuzzleData(),
                        progress = currentProgress(),
                        results = emptyList(),
                        promotion = null,
                    )
                }
            } catch (_: Exception) {
                _uiState.value = FailedPuzzlesUiState.Failed
            }
        }
    }

    private fun loadPuzzleIntoInteractor(puzzle: Puzzle) {
        puzzleInteractor.load(puzzle)
        boardViewBuilder.load(puzzle)
    }

    override fun onSquareClicked(selection: Locus) {
        val state = _uiState.value
        if (state !is FailedPuzzlesUiState.Playing) return

        if (boardViewBuilder.selected != null) {
            val current = boardViewBuilder.selected!!
            if (current == selection || !puzzleInteractor.canPlay(current, selection)) {
                boardViewBuilder.clearSelection()
                updatePlayingState(state)
            } else if (puzzleInteractor.canPromote(current, selection)) {
                _uiState.value = state.copy(
                    promotion = FailedPuzzlesUiState.Playing.Promotion(
                        showChooser = true,
                        at = selection
                    )
                )
            } else {
                puzzleInteractor.play(current, selection)
                refreshBoardWithAnimation()
                handleMoveResult(state)
            }
        } else {
            boardViewBuilder.withSelection(selection, puzzleInteractor.moves(selection))
            updatePlayingState(state)
        }
    }

    override fun onPromote(to: Piece) {
        val state = _uiState.value
        if (state !is FailedPuzzlesUiState.Playing) return

        puzzleInteractor.promote(boardViewBuilder.selected!!, state.promotion!!.at, to)
        refreshBoardWithAnimation()
        handleMoveResult(state)
    }

    override fun onDismissCompletion() {
        val currentState = _uiState.value
        if (currentState is FailedPuzzlesUiState.Finished) {
            _uiState.value = currentState.copy(showCompletionDialog = false)
        }
    }

    private fun handleMoveResult(currentState: FailedPuzzlesUiState.Playing) {
        if (puzzleInteractor.isOver()) {
            val success = puzzleInteractor.isSuccess()
            val newResult = FailedPuzzlesUiState.PuzzleResult(
                id = puzzleInteractor.id,
                rating = puzzleInteractor.rating,
                success = success
            )
            val updatedResults = currentState.results + newResult

            if (success) {
                solvedCount++
                // Remove from failed puzzles list
                viewModelScope.launch {
                    puzzleInteractor.id?.let { onFailedPuzzleComplete(it) }
                }
            }

            // Move to next puzzle or finish
            viewModelScope.launch {
                val nextPuzzle = getFailedPuzzles.next()
                if (nextPuzzle != null) {
                    loadPuzzleIntoInteractor(nextPuzzle)
                    _uiState.value = FailedPuzzlesUiState.Playing(
                        data = currentPuzzleData(),
                        progress = currentProgress(),
                        results = updatedResults,
                        promotion = null,
                    )
                } else {
                    // All puzzles completed
                    _uiState.value = FailedPuzzlesUiState.Finished(
                        data = currentPuzzleData(),
                        progress = currentProgress(),
                        results = updatedResults,
                        showCompletionDialog = true,
                    )
                }
            }
        } else {
            updatePlayingState(currentState)
        }
    }

    private fun refreshBoardWithAnimation() {
        boardViewBuilder.refresh()
        puzzleInteractor.lastPly?.let { lastPly ->
            boardViewBuilder.withAnimatingPiece(lastPly.from, lastPly.to)
        }
    }

    private fun updatePlayingState(currentState: FailedPuzzlesUiState.Playing) {
        _uiState.value = currentState.copy(
            data = currentPuzzleData(),
            promotion = null,
        )
    }

    private fun currentPuzzleData(): FailedPuzzlesUiState.PuzzleData =
        FailedPuzzlesUiState.PuzzleData(
            rating = puzzleInteractor.rating,
            player = puzzleInteractor.player,
            boardData = boardViewBuilder.build(),
        )

    private fun currentProgress(): FailedPuzzlesUiState.Progress =
        FailedPuzzlesUiState.Progress(
            solved = solvedCount,
            total = getFailedPuzzles.totalCount(),
        )
}
