package com.paulcraciunas.screens.puzzles.failed.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.domain.api.puzzles.OnFailedPuzzleComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleAnalysisData
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.board.PIECE_MOVE_ANIMATION_DURATION_MS
import com.paulcraciunas.screens.common.model.BoardInteractionHelper
import com.paulcraciunas.screens.common.model.ClickResult
import com.paulcraciunas.screens.common.model.PlayableData
import com.paulcraciunas.screens.common.model.PuzzlePlayableBoard
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FailedPuzzlesViewModel @Inject constructor(
    private val getFailedPuzzles: GetFailedPuzzles,
    private val onFailedPuzzleComplete: OnFailedPuzzleComplete,
    private val getPuzzleFen: GetPuzzleFen,
    private val appSettingsRepository: AppSettingsRepository,
    private val timer: Timer,
) : ViewModel() {
    private val helper = BoardInteractionHelper()
    private var enableAnimations: Boolean = true

    private val _navigateToAnalysis = Channel<PuzzleAnalysisData>(Channel.BUFFERED)
    val navigateToAnalysis: Flow<PuzzleAnalysisData> = _navigateToAnalysis.receiveAsFlow()

    private val _uiState = MutableStateFlow<FailedPuzzlesUiState>(FailedPuzzlesUiState.Loading)
    val uiState: StateFlow<FailedPuzzlesUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        loadPuzzles()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { settings ->
                helper.autoPromote = settings.autoPromote
                enableAnimations = settings.enableAnimations
            }
        }
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
                getFailedPuzzles.load(viewModelScope)
                val puzzle = getFailedPuzzles.next()
                _uiState.update {
                    if (puzzle == null) FailedPuzzlesUiState.Empty
                    else FailedPuzzlesUiState.Playing(
                        data = helper.load(PuzzlePlayableBoard(puzzle)),
                        progress = FailedPuzzlesUiState.Progress(solved = 0, total = getFailedPuzzles.totalCount()),
                        results = emptyList(),
                        promotion = null,
                    )
                }
                whilePlaying {
                    timer.start()
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load failed puzzles")
                _uiState.update { FailedPuzzlesUiState.Failed }
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

    fun onDismissCompletion() {
        _uiState.update { state ->
            if (state is FailedPuzzlesUiState.Finished) state.copy(showCompletionDialog = false)
            else state
        }
    }

    fun onAnalyzeFailedPuzzle(puzzleId: Int) {
        viewModelScope.launch {
            getPuzzleFen(puzzleId)?.let { data -> _navigateToAnalysis.send(data) }
                ?: Timber.w("Failed to get puzzle fen")
        }
    }

    private fun handleMoveResult(result: ClickResult) {
        _uiState.updatePlaying { it.copy(data = result.data, promotion = result.promotion, isAnimating = result.data.isOver) }
        if (result.data.isOver) {
            onPuzzleCompleted(result.data, result.data.won)
        }
    }

    private fun onPuzzleCompleted(data: PlayableData, isSuccess: Boolean) = whilePlaying { state ->
        viewModelScope.launch {
            if (isSuccess) {
                data.id?.let { onFailedPuzzleComplete(it, timer.elapsed()) }
            }
            if (enableAnimations) {
                delay(ANIMATION_WAIT_MS)
            }
            val updatedResults = state.results + PuzzleResult(id = data.id, rating = data.rating!!, success = isSuccess)
            val progress = state.progress.copy(solved = updatedResults.count { it.success })
            val nextPuzzle = getFailedPuzzles.next()
            _uiState.update {
                if (nextPuzzle != null) {
                    FailedPuzzlesUiState.Playing(
                        data = helper.load(PuzzlePlayableBoard(nextPuzzle)),
                        progress = progress,
                        results = updatedResults,
                        promotion = null,
                    )
                } else FailedPuzzlesUiState.Finished(
                    data = data,
                    progress = progress,
                    results = updatedResults,
                    showCompletionDialog = true,
                )
            }
            whilePlaying {
                timer.start()
            }
        }
    }

    companion object {
        internal const val ANIMATION_WAIT_MS = PIECE_MOVE_ANIMATION_DURATION_MS + 50L
    }

    private fun whilePlaying(block: (FailedPuzzlesUiState.Playing) -> Unit) {
        val state = _uiState.value
        if (state !is FailedPuzzlesUiState.Playing) return
        block(state)
    }

    private fun whilePlayingInteractive(block: (FailedPuzzlesUiState.Playing) -> Unit) {
        val state = _uiState.value
        if (state !is FailedPuzzlesUiState.Playing || state.isAnimating) return
        block(state)
    }

    private inline fun MutableStateFlow<FailedPuzzlesUiState>.updatePlaying(
        crossinline function: (FailedPuzzlesUiState.Playing) -> FailedPuzzlesUiState,
    ) = update { state ->
        if (state !is FailedPuzzlesUiState.Playing) state else function(state)
    }
}
