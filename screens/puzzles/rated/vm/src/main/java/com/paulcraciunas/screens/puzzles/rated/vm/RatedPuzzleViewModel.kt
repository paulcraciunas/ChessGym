package com.paulcraciunas.screens.puzzles.rated.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.EloResult
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.domain.api.puzzles.OnPuzzleComplete
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.PlaySession
import com.paulcraciunas.screens.data.engine.PlaySessionState
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatedPuzzleViewModel @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val timer: Timer,
    getRatedPuzzle: GetRatedPuzzle,
    onPuzzleComplete: OnPuzzleComplete,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val ratingChange = MutableStateFlow(EloResult(0, 0))
    private val playSession = PlaySession(
        settingsRepository = appSettingsRepository,
        config = ratedConfiguration(),
        sessions = RatedPuzzleSessions(getRatedPuzzle, ratingChange),
        onSessionComplete = RatedPuzzleOnComplete(onPuzzleComplete, ratingChange, timer),
    )

    val uiState: StateFlow<RatedPuzzleUiState> = combine(
        playSession.state,
        ratingChange,
    ) { state, elo -> state.toUiState(elo) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RatedPuzzleUiState.Loading,
        )

    init {
        timer.start()
        viewModelScope.launch(defaultDispatcher) { playSession.run() }
    }

    fun onStop() { timer.pause() }
    fun onStart() { timer.resume() }
    fun onSquareClicked(selection: Locus) = playSession.accept(PlayIntent.SelectSquare(selection))
    fun onPromote(to: Piece) = playSession.accept(PlayIntent.Promote(to))
    fun onHintRequested() = playSession.accept(PlayIntent.Hint)
    fun onAbandon() = playSession.accept(PlayIntent.RequestAbandon)
    fun onAbandonDismissed() = playSession.accept(PlayIntent.DismissAbandon)
    fun onAbandonConfirmed() = playSession.accept(PlayIntent.ConfirmAbandon)
    fun onNextPuzzle() = playSession.accept(PlayIntent.Resume)

    fun onNavigateBackPressed(): Boolean {
        if (playSession.state.value.status == PlaySessionState.Status.Playing) {
            playSession.accept(PlayIntent.RequestAbandon)
        }
        return playSession.state.value.status == PlaySessionState.Status.Playing
    }

    private fun PlaySessionState.toUiState(elo: EloResult): RatedPuzzleUiState = when (status) {
        PlaySessionState.Status.Failed -> RatedPuzzleUiState.Failed
        PlaySessionState.Status.Loading -> RatedPuzzleUiState.Loading
        PlaySessionState.Status.Ended,
        PlaySessionState.Status.Paused -> RatedPuzzleUiState.Finished(
            rating = boardState.rating ?: 0,
            data = boardState,
            success = boardState.won,
            ratingChange = elo.get(success = boardState.won),
        )
        PlaySessionState.Status.Ready,
        PlaySessionState.Status.Playing -> RatedPuzzleUiState.Playing(
            rating = boardState.rating ?: 0,
            data = boardState,
            hintEnabled = hintAvailable,
            showAbandonDialog = abandonRequested,
        )
    }
}
