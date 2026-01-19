package com.paulcraciunas.screens.puzzles.rated.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.EloResult
import com.paulcraciunas.domain.api.GetRatedPuzzle
import com.paulcraciunas.domain.api.OnPuzzleComplete
import com.paulcraciunas.domain.api.PuzzleCompletionResult
import com.paulcraciunas.domain.api.Timer
import com.paulcraciunas.game.logic.api.GameFactory
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
class RatedPuzzleViewModel @Inject constructor(
    private val getRatedPuzzle: GetRatedPuzzle,
    private val onPuzzleComplete: OnPuzzleComplete,
    private val timer: Timer,
    gameFactory: GameFactory,
) : ViewModel(), RatedPuzzleScreenInteractor {
    private val boardViewBuilder = BoardViewDataBuilder()
    private val puzzleInteractor = gameFactory.puzzleInteractor()

    private val _uiState = MutableStateFlow<RatedPuzzleUiState>(RatedPuzzleUiState.Loading)
    val uiState: StateFlow<RatedPuzzleUiState> = _uiState.asStateFlow()
    private var puzzleData: GetRatedPuzzle.Data? = null

    init {
        loadPuzzle()
    }

    private fun loadPuzzle() {
        viewModelScope.launch {
            try {
                puzzleData = getRatedPuzzle()
                puzzleInteractor.load(puzzleData!!.puzzle)
                boardViewBuilder.load(puzzleData!!.puzzle)
                refreshBoardWithAnimation()
                timer.start()
                _uiState.value = RatedPuzzleUiState.Playing(
                    data = RatedPuzzleUiState.PuzzleData(
                        rating = puzzleInteractor.rating,
                        player = puzzleInteractor.player,
                        boardData = boardViewBuilder.build(),
                        captured = puzzleInteractor.captured
                    ),
                    hintEnabled = true,
                    showAbandonDialog = false,
                    promotion = null
                )
            } catch (_: Exception) {
                _uiState.value = RatedPuzzleUiState.Failed
            }
        }
    }

    override fun onSquareClicked(selection: Locus) {
        if (boardViewBuilder.selected != null) {
            val current = boardViewBuilder.selected!!
            // Are we selecting the same location, or something we can't move to?
            if (current == selection || !puzzleInteractor.canPlay(current, selection)) {
                boardViewBuilder.clearSelection()
                _uiState.value = playingState.copy(data = updatedBoardData())
            } else if (puzzleInteractor.canPromote(current, selection)) {
                _uiState.value = playingState.copy(promotion = RatedPuzzleUiState.Playing.Promotion(showChooser = true, at = selection))
            } else {
                // If we can move to this location, and we don't require promotion, play it
                puzzleInteractor.play(current, selection)
                refreshBoardWithAnimation()
                updateState(playingState.copy(data = updatedBoardData(), promotion = null))
            }
        } else { // Otherwise, we have a new selected square
            boardViewBuilder.withSelection(selection, puzzleInteractor.moves(selection))
            _uiState.value = playingState.copy(data = updatedBoardData())
        }
    }

    override fun onPromote(to: Piece) {
        puzzleInteractor.promote(boardViewBuilder.selected!!, playingState.promotion!!.at, to)
        refreshBoardWithAnimation()
        updateState(playingState.copy(data = updatedBoardData(), promotion = null))
    }

    override fun onHintRequested() {
        val selection = puzzleInteractor.hint()
        val newMoves = puzzleInteractor.moves(selection)
        boardViewBuilder.withSelection(selection, newMoves)
        _uiState.value = playingState.copy(data = updatedBoardData())
    }

    override fun onAbandon() {
        _uiState.value = playingState.copy(showAbandonDialog = true)
    }

    override fun onAbandonConfirmed() {
        puzzleInteractor.resign()
        updateState(playingState)
    }

    override fun onAbandonDismissed() {
        _uiState.value = playingState.copy(showAbandonDialog = false)
    }

    override fun onNextPuzzle() {
        loadPuzzle()
    }

    // Returns true if back press was handled, false otherwise
    fun onNavigateBackPressed(): Boolean {
        if (_uiState.value is RatedPuzzleUiState.Playing) {
            onAbandon()
            return true
        }
        return false
    }

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    private fun updateState(state: RatedPuzzleUiState.Playing) {
        if (puzzleInteractor.isOver()) {
            finishPuzzle(state)
        } else {
            _uiState.value = state
        }
    }

    private fun finishPuzzle(state: RatedPuzzleUiState.Playing) {
        val success = puzzleInteractor.isSuccess()
        _uiState.value = RatedPuzzleUiState.Finished(
            data = state.data.copy(),
            success = success,
            ratingChange = puzzleData!!.ratingChange.get(success = success),
        )
        logResult(success, puzzleData!!.ratingChange)
    }

    private fun logResult(success: Boolean, eloResult: EloResult) {
        viewModelScope.launch {
            onPuzzleComplete(
                PuzzleCompletionResult(
                    puzzleId = puzzleInteractor.id,
                    puzzleRating = puzzleInteractor.rating,
                    wasSuccessful = success,
                    ratingChange = eloResult.get(success = success),
                    timeSpentMillis = timer.elapsed(),
                )
            )
        }
    }

    private fun refreshBoardWithAnimation() {
        boardViewBuilder.refresh()
        puzzleInteractor.lastPly?.let { lastPly ->
            boardViewBuilder.withAnimatingPiece(lastPly.from, lastPly.to)
        }
    }

    private fun updatedBoardData(): RatedPuzzleUiState.PuzzleData =
        playingState.data.copy(boardData = boardViewBuilder.build())

    private val playingState: RatedPuzzleUiState.Playing
        get() = _uiState.value as RatedPuzzleUiState.Playing
}
