package com.paulcraciunas.screens.puzzles.rated.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.EloResult
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.domain.api.puzzles.OnPuzzleComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleCompletionResult
import com.paulcraciunas.domain.api.general.Timer
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
class RatedPuzzleViewModel @Inject constructor(
    private val getRatedPuzzle: GetRatedPuzzle,
    private val onPuzzleComplete: OnPuzzleComplete,
    private val timer: Timer,
    puzzleInteractor: PuzzleInteractor,
) : ViewModel(), RatedPuzzleScreenInteractor {
    private val helper = PuzzleViewModelHelper(puzzleInteractor = puzzleInteractor)

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
            } catch (e: Exception) {
                Timber.w(e, "Failed to load rated puzzle")
                _uiState.value = RatedPuzzleUiState.Failed
            }
        }
    }

    override fun onSquareClicked(selection: Locus) = whilePlayingInteractive {
        handleMoveResult(helper.handleSquareClick(selection))
    }

    override fun onPromote(to: Piece) = whilePlayingInteractive { state ->
        if (state.promotion == null) return@whilePlayingInteractive
        handleMoveResult(helper.promote(to, state.promotion.at))
    }

    override fun onHintRequested() = whilePlayingInteractive { state ->
        helper.hint()
        _uiState.value = state.copy(data = helper.buildPuzzleData(), hintEnabled = false)
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
                val canUpdate = currentState is RatedPuzzleUiState.Playing && currentState.isShowingSolution
                canUpdate.also { if (canUpdate) _uiState.value = currentState.copy(data = data) }
            }

            // After solution shown, finish the puzzle as failed
            finishPuzzle(isSuccess = false)
        }
    }

    override fun onAbandonDismissed() = whilePlayingInteractive { state ->
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

    /** Same as [whilePlaying] but also blocks interaction while showing solution */
    private fun whilePlayingInteractive(block: (RatedPuzzleUiState.Playing) -> Unit) {
        val state = _uiState.value
        if (state !is RatedPuzzleUiState.Playing || state.isShowingSolution) return

        block(state)
    }
}
