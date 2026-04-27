package com.paulcraciunas.screens.puzzles.failed.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.domain.api.puzzles.OnFailedPuzzleComplete
import com.paulcraciunas.game.logic.api.PuzzleInteractor
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper.OnSquareClick
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FailedPuzzlesViewModel @Inject constructor(
    private val getFailedPuzzles: GetFailedPuzzles,
    private val onFailedPuzzleComplete: OnFailedPuzzleComplete,
    private val getPuzzleFen: GetPuzzleFen,
    private val timer: Timer,
    puzzleInteractor: PuzzleInteractor,
) : ViewModel(), FailedPuzzlesScreenInteractor {
    private val helper = PuzzleViewModelHelper(puzzleInteractor = puzzleInteractor)

    private val _navigateToAnalysis = Channel<String>(Channel.BUFFERED)
    val navigateToAnalysis: Flow<String> = _navigateToAnalysis.receiveAsFlow()

    private val _uiState = MutableStateFlow<FailedPuzzlesUiState>(FailedPuzzlesUiState.Loading)
    val uiState: StateFlow<FailedPuzzlesUiState> = _uiState.asStateFlow()

    private var solvedCount: Int = 0

    init {
        loadPuzzles()
    }

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
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
                    timer.start()
                    _uiState.value = FailedPuzzlesUiState.Playing(
                        data = helper.load(puzzle),
                        progress = currentProgress(),
                        results = emptyList(),
                        promotion = null,
                    )
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load failed puzzles")
                _uiState.value = FailedPuzzlesUiState.Failed
            }
        }
    }

    override fun onSquareClicked(selection: Locus) = whilePlaying { _ ->
        handleMoveResult(helper.handleSquareClick(selection))
    }

    override fun onPromote(to: Piece) = whilePlaying { state->
        state.promotion?.let {
            handleMoveResult(helper.promote(to, state.promotion.at))
        }
    }

    override fun onDismissCompletion() {
        val currentState = _uiState.value
        if (currentState is FailedPuzzlesUiState.Finished) {
            _uiState.value = currentState.copy(showCompletionDialog = false)
        }
    }

    override fun onAnalyzeFailedPuzzle(puzzleId: Int) {
        viewModelScope.launch {
            getPuzzleFen(puzzleId)?.let { fen -> _navigateToAnalysis.send(fen) } ?: Timber.w("Failed to get puzzle fen")
        }
    }

    private fun handleMoveResult(result: OnSquareClick) = whilePlaying { state ->
        when {
            result.promotion != null -> _uiState.value = state.copy(promotion = result.promotion)
            !result.isOver -> _uiState.value = state.copy(data = helper.buildPuzzleData(), promotion = null)
            else -> handlePuzzleOver(result.isSuccess)
        }
    }

    private fun handlePuzzleOver(isSuccess: Boolean) = whilePlaying { state ->
        val timeSpent = timer.elapsed()
        val updatedResults = state.results + PuzzleResult(
            id = helper.id,
            rating = helper.rating,
            success = isSuccess
        )

        if (isSuccess) {
            solvedCount++
            viewModelScope.launch {
                helper.id?.let { onFailedPuzzleComplete(it, timeSpent) }
            }
        }

        viewModelScope.launch {
            val nextPuzzle = getFailedPuzzles.next()
            if (nextPuzzle != null) {
                timer.start()
                _uiState.value = FailedPuzzlesUiState.Playing(
                    data = helper.load(nextPuzzle),
                    progress = currentProgress(),
                    results = updatedResults,
                    promotion = null,
                )
            } else {
                _uiState.value = FailedPuzzlesUiState.Finished(
                    data = helper.buildPuzzleData(),
                    progress = currentProgress(),
                    results = updatedResults,
                    showCompletionDialog = true,
                )
            }
        }
    }

    private fun currentProgress(): FailedPuzzlesUiState.Progress =
        FailedPuzzlesUiState.Progress(
            solved = solvedCount,
            total = getFailedPuzzles.totalCount(),
        )

    private fun whilePlaying(block: (FailedPuzzlesUiState.Playing) -> Unit) {
        val state = _uiState.value
        if (state !is FailedPuzzlesUiState.Playing) return
        block(state)
    }
}
