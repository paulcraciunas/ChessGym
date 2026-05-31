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
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.PuzzleSessionFactory
import com.paulcraciunas.screens.data.SessionSettings
import com.paulcraciunas.screens.data.runAs
import com.paulcraciunas.screens.data.updateAs
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RatedPuzzleViewModel @Inject constructor(
    private val getRatedPuzzle: GetRatedPuzzle,
    private val onPuzzleComplete: OnPuzzleComplete,
    appSettingsRepository: AppSettingsRepository,
    private val timer: Timer,
) : ViewModel() {
    private val factory = PuzzleSessionFactory()
    private val session = factory.get()

    private val _uiState = MutableStateFlow<RatedPuzzleUiState>(RatedPuzzleUiState.Loading)
    val uiState: StateFlow<RatedPuzzleUiState> = _uiState.asStateFlow()
    private lateinit var result: EloResult

    init {
        session.bindSettings(viewModelScope, appSettingsRepository.appSettings.map {
            SessionSettings(it.autoPromote, it.enableAnimations)
        })
        observeBoardState()
        loadPuzzle()
    }

    private fun observeBoardState() {
        viewModelScope.launch {
            session.data.collect { boardState ->
                _uiState.updateAs { it: RatedPuzzleUiState.Playing -> it.copy(data = boardState) }
                _uiState.runAs<RatedPuzzleUiState.Playing> {
                    if (boardState.isOver && !it.isShowingSolution) {
                        finishPuzzle(boardState, boardState.won)
                    }
                }
            }
        }
    }

    private fun loadPuzzle() {
        viewModelScope.launch {
            try {
                val puzzleData = getRatedPuzzle()
                result = puzzleData.ratingChange
                factory.load(viewModelScope, puzzleData.puzzle)
                timer.start()
                _uiState.update { RatedPuzzleUiState.Playing(data = session.currentState, hintEnabled = true, showAbandonDialog = false) }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load rated puzzle")
                _uiState.update { RatedPuzzleUiState.Failed }
            }
        }
    }

    fun onSquareClicked(selection: Locus) = _uiState.runAs<RatedPuzzleUiState.Playing> {
        if (!it.isShowingSolution) session.onClick(selection)
    }

    fun onPromote(to: Piece) = session.promoteIfPending(to)
    fun onHintRequested() = _uiState.runAs<RatedPuzzleUiState.Playing> {
        if (it.hintEnabled && !it.isShowingSolution) {
            session.hint()
            _uiState.updateAs { it: RatedPuzzleUiState.Playing -> it.copy(hintEnabled = false) }
        }
    }

    fun onAbandon() = _uiState.updateAs { it: RatedPuzzleUiState.Playing -> it.copy(showAbandonDialog = true) }
    fun onAbandonDismissed() = _uiState.updateAs { it: RatedPuzzleUiState.Playing -> it.copy(showAbandonDialog = false) }
    fun onAbandonConfirmed() {
        _uiState.updateAs { it: RatedPuzzleUiState.Playing -> it.copy(showAbandonDialog = false, isShowingSolution = true) }

        viewModelScope.launch {
            session.playSolution { _uiState.value is RatedPuzzleUiState.Playing }
            finishPuzzle(session.currentState, isSuccess = false)
        }
    }

    fun onNextPuzzle() {
        loadPuzzle()
    }

    fun onNavigateBackPressed(): Boolean {
        _uiState.runAs<RatedPuzzleUiState.Playing> { onAbandon() }
        return _uiState.value is RatedPuzzleUiState.Playing
    }

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    private fun finishPuzzle(data: BoardState, isSuccess: Boolean) = _uiState.runAs<RatedPuzzleUiState.Playing> {
        _uiState.update {
            RatedPuzzleUiState.Finished(
                data = data,
                success = isSuccess,
                ratingChange = result.get(success = isSuccess),
            )
        }
        logResult(data, isSuccess)
    }

    private fun logResult(data: BoardState, success: Boolean) {
        viewModelScope.launch {
            onPuzzleComplete(
                PuzzleCompletionResult(
                    puzzleId = data.id,
                    puzzleRating = data.rating!!,
                    wasSuccessful = success,
                    ratingChange = result.get(success = success),
                    timeSpentMillis = timer.elapsed(),
                )
            )
        }
    }
}
