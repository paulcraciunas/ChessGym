package com.paulcraciunas.screens.puzzles.streak.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakPuzzleComplete
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.PuzzleSessionFactory
import com.paulcraciunas.screens.data.SessionSettings
import com.paulcraciunas.screens.data.runAs
import com.paulcraciunas.screens.data.updateAs
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PuzzleStreakViewModel @Inject constructor(
    private val getStreakPuzzle: GetStreakPuzzle,
    private val onStreakPuzzleComplete: OnStreakPuzzleComplete,
    private val onStreakComplete: OnStreakComplete,
    private val appSettingsRepository: AppSettingsRepository,
    private val timer: Timer,
) : ViewModel() {
    private val factory = PuzzleSessionFactory()
    private val session = factory.get()
    private val _uiState = MutableStateFlow<PuzzleStreakUiState>(PuzzleStreakUiState.Loading)
    val uiState: StateFlow<PuzzleStreakUiState> = _uiState.asStateFlow()

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
                _uiState.updateAs { it: PuzzleStreakUiState.Playing -> it.copy(data = boardState) }
                _uiState.runAs<PuzzleStreakUiState.Playing> {
                    if (boardState.isOver && boardState.interactive && !it.isShowingSolution && !it.isAwaitingNextPuzzle) {
                        onPuzzleCompleted()
                    }
                }
            }
        }
    }

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    private fun loadPuzzle() {
        viewModelScope.launch {
            try {
                loadNextPuzzle()
            } catch (e: Exception) {
                Timber.w(e, "Failed to load streak puzzle")
                _uiState.update { PuzzleStreakUiState.Failed }
            }
        }
    }

    fun onSquareClicked(selection: Locus) = whilePlayingInteractive { session.onClick(selection) }
    fun onPromote(to: Piece) = whilePlayingInteractive { session.promoteIfPending(to) }
    fun onHintRequested() = whilePlayingInteractive { state ->
        if (state.hintEnabled) {
            session.hint()
            _uiState.updateAs { it: PuzzleStreakUiState.Playing -> it.copy(hintEnabled = false) }
        }
    }

    fun onAbandon() = _uiState.updateAs { it: PuzzleStreakUiState.Playing -> it.copy(showAbandonDialog = true) }
    fun onAbandonConfirmed() = _uiState.runAs<PuzzleStreakUiState.Playing> {
        _uiState.updateAs { it: PuzzleStreakUiState.Playing -> it.copy(showAbandonDialog = false, isShowingSolution = true) }

        viewModelScope.launch {
            session.playSolution { _uiState.value is PuzzleStreakUiState.Playing }
            endStreak()
        }
    }

    fun onAbandonDismissed() = _uiState.updateAs { it: PuzzleStreakUiState.Playing -> it.copy(showAbandonDialog = false) }
    fun onNewStreak() {
        _uiState.update { PuzzleStreakUiState.Loading }
        loadPuzzle()
    }

    fun onDismissSummary() {
        _uiState.update { state ->
            if (state is PuzzleStreakUiState.StreakEnded) state.copy(showSummary = false)
            else state
        }
    }

    fun onNextPuzzle() = _uiState.runAs<PuzzleStreakUiState.Playing> {
        if (it.isAwaitingNextPuzzle) {
            viewModelScope.launch {
                goToNext()
            }
        }
    }

    private fun onPuzzleCompleted() = _uiState.runAs<PuzzleStreakUiState.Playing> {
        viewModelScope.launch {
            if (session.currentState.won) {
                onStreakPuzzleComplete(timer.elapsed())
                if (appSettingsRepository.appSettings.first().autoNextPuzzle) goToNext()
                else _uiState.updateAs { it: PuzzleStreakUiState.Playing -> it.copy(isAwaitingNextPuzzle = true) }
            } else {
                endStreak()
            }
        }
    }

    private suspend fun goToNext() {
        try {
            loadNextPuzzle()
        } catch (e: Exception) {
            Timber.w(e, "Failed to load next streak puzzle, ending streak")
            endStreak()
        }
    }

    private suspend fun loadNextPuzzle() {
        timer.start()
        val data = getStreakPuzzle()
        factory.load(viewModelScope, data.puzzle)
        _uiState.update {
            PuzzleStreakUiState.Playing(
                data = session.currentState,
                streakCount = data.currentStreakCount,
                isAwaitingNextPuzzle = false,
                hintEnabled = true,
                showAbandonDialog = false,
            )
        }
    }

    private suspend fun endStreak() {
        val timeSpent = timer.elapsed()
        val result = onStreakComplete(timeSpent)
        _uiState.updateAs { _: PuzzleStreakUiState.Playing ->
            PuzzleStreakUiState.StreakEnded(
                data = session.currentState,
                finalStreakCount = result.finalStreakCount,
                isNewHighScore = result.isNewHighScore,
                showSummary = true,
            )
        }
    }

    private inline fun whilePlayingInteractive(block: (PuzzleStreakUiState.Playing) -> Unit) = _uiState.runAs<PuzzleStreakUiState.Playing> {
        if (!it.isShowingSolution && !it.isAwaitingNextPuzzle) block(it)
    }
}
