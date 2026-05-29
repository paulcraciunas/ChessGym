package com.paulcraciunas.screens.puzzles.rated.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.EloResult
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.domain.api.puzzles.OnPuzzleComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleCompletionResult
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.BoardInteractionHelper
import com.paulcraciunas.screens.data.ClickResult
import com.paulcraciunas.screens.data.PlayableData
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.screens.data.PuzzleSolution
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RatedPuzzleViewModel @Inject constructor(
    private val getRatedPuzzle: GetRatedPuzzle,
    private val onPuzzleComplete: OnPuzzleComplete,
    private val appSettingsRepository: AppSettingsRepository,
    private val timer: Timer,
) : ViewModel() {
    private val helper = BoardInteractionHelper(solution = PuzzleSolution())

    private val _uiState = MutableStateFlow<RatedPuzzleUiState>(RatedPuzzleUiState.Loading)
    val uiState: StateFlow<RatedPuzzleUiState> = _uiState.asStateFlow()
    private lateinit var puzzleData: GetRatedPuzzle.Data

    init {
        observeSettings()
        loadPuzzle()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { settings ->
                helper.autoPromote = settings.autoPromote
            }
        }
    }

    private fun loadPuzzle() {
        viewModelScope.launch {
            try {
                puzzleData = getRatedPuzzle()
                val data = helper.load(PuzzlePlayableBoard(puzzleData.puzzle))
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

    fun onSquareClicked(selection: Locus) = whilePlayingInteractive {
        handleMoveResult(helper.handleSquareClick(selection))
    }

    fun onPromote(to: Piece) = whilePlayingInteractive { state ->
        state.promotion?.let {
            handleMoveResult(helper.promote(to, state.promotion.at))
        }
    }

    fun onHintRequested() = _uiState.updateInteractive { state ->
        if (!state.hintEnabled) state
        else state.copy(data = helper.hint(), hintEnabled = false)
    }

    fun onAbandon() = _uiState.updateInteractive { it.copy(showAbandonDialog = true) }
    fun onAbandonDismissed() = _uiState.updateInteractive { it.copy(showAbandonDialog = false) }
    fun onAbandonConfirmed() {
        _uiState.updateInteractive {
            it.copy(
                showAbandonDialog = false,
                isShowingSolution = true,
            )
        }

        whilePlaying { state ->
            viewModelScope.launch {
                helper.playSolution { data ->
                    _uiState.updatePlaying { it.copy(data = data) }
                    return@playSolution _uiState.value is RatedPuzzleUiState.Playing
                }
                finishPuzzle(state.data, isSuccess = false)
            }
        }
    }

    fun onNextPuzzle() {
        loadPuzzle()
    }

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

    private fun handleMoveResult(result: ClickResult) =
        if (!result.data.isOver) _uiState.updatePlaying { it.copy(data = result.data, promotion = result.promotion) }
        else finishPuzzle(result.data, result.data.won)

    private fun finishPuzzle(data: PlayableData, isSuccess: Boolean) {
        _uiState.updatePlaying {
            RatedPuzzleUiState.Finished(
                data = data,
                success = isSuccess,
                ratingChange = puzzleData.ratingChange.get(success = isSuccess),
            )
        }
        logResult(data, isSuccess, puzzleData.ratingChange)
    }

    private fun logResult(data: PlayableData, success: Boolean, eloResult: EloResult) {
        viewModelScope.launch {
            onPuzzleComplete(
                PuzzleCompletionResult(
                    puzzleId = data.id,
                    puzzleRating = data.rating!!,
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

    private inline fun MutableStateFlow<RatedPuzzleUiState>.updatePlaying(
        crossinline function: (RatedPuzzleUiState.Playing) -> RatedPuzzleUiState,
    ) = update { state ->
        if (state !is RatedPuzzleUiState.Playing) state else function(state)
    }

    private inline fun MutableStateFlow<RatedPuzzleUiState>.updateInteractive(
        crossinline function: (RatedPuzzleUiState.Playing) -> RatedPuzzleUiState,
    ) = update { state ->
        if (state !is RatedPuzzleUiState.Playing || state.isShowingSolution) state
        else function(state)
    }
}
