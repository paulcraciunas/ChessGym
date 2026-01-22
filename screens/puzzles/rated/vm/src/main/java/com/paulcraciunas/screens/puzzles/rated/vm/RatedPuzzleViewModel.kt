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
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper.OnSquareClick
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
    private val helper = PuzzleViewModelHelper(puzzleInteractor = gameFactory.puzzleInteractor())

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
                val data = helper.load(puzzleData!!.puzzle)
                timer.start()
                _uiState.value = RatedPuzzleUiState.Playing(
                    data = data,
                    hintEnabled = true,
                    showAbandonDialog = false,
                    promotion = null
                )
            } catch (_: Exception) {
                _uiState.value = RatedPuzzleUiState.Failed
            }
        }
    }

    override fun onSquareClicked(selection: Locus) = whilePlaying {
        handleMoveResult(helper.handleSquareClick(selection))
    }

    override fun onPromote(to: Piece) {
        val state = _uiState.value
        if (state !is RatedPuzzleUiState.Playing || state.promotion == null) return

        handleMoveResult(helper.promote(to, state.promotion.at))
    }

    override fun onHintRequested() = whilePlaying { state ->
        helper.hint()
        _uiState.value = state.copy(data = helper.buildPuzzleData(), hintEnabled = false)
    }

    override fun onAbandon() = whilePlaying { state ->
        _uiState.value = state.copy(showAbandonDialog = true)
    }

    override fun onAbandonConfirmed() = whilePlaying {
        helper.resign()
        finishPuzzle(isSuccess = false)
    }

    override fun onAbandonDismissed() = whilePlaying { state ->
        _uiState.value = state.copy(showAbandonDialog = false)
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

    private fun handleMoveResult(result: OnSquareClick) = whilePlaying { state ->
        when {
            result.promotion != null -> _uiState.value = state.copy(promotion = result.promotion)
            !result.isOver -> _uiState.value = state.copy(data = result.data, promotion = null)
            else -> finishPuzzle(result.isSuccess)
        }
    }

    private fun finishPuzzle(isSuccess: Boolean) {
        _uiState.value = RatedPuzzleUiState.Finished(
            data = helper.buildPuzzleData(),
            success = isSuccess,
            ratingChange = puzzleData!!.ratingChange.get(success = isSuccess),
        )
        logResult(isSuccess, puzzleData!!.ratingChange)
    }

    private fun logResult(success: Boolean, eloResult: EloResult) {
        viewModelScope.launch {
            onPuzzleComplete(
                PuzzleCompletionResult(
                    puzzleId = helper.id,
                    puzzleRating = helper.rating,
                    wasSuccessful = success,
                    ratingChange = eloResult.get(success = success),
                    timeSpentMillis = timer.elapsed(),
                )
            )
        }
    }

    private fun whilePlaying(block: (RatedPuzzleUiState.Playing) -> Unit) {
        val state = _uiState.value
        if (state !is RatedPuzzleUiState.Playing) return

        block(state)
    }
}
