package com.paulcraciunas.screens.puzzles.failed.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.domain.api.puzzles.OnFailedPuzzleComplete
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.navigation.NavigationDispatcher
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.PlaySession
import com.paulcraciunas.screens.data.engine.PlaySessionState
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FailedPuzzlesViewModel @Inject constructor(
    private val timer: Timer,
    private val navDispatcher: NavigationDispatcher,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    getFailedPuzzles: GetFailedPuzzles,
    onFailedPuzzleComplete: OnFailedPuzzleComplete,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val playSession = PlaySession(
        settingsRepository = appSettingsRepository,
        config = failedPuzzlesConfiguration(),
        sessions = FailedPuzzlesSessions(getFailedPuzzles),
        onSessionComplete = { onFailedPuzzleComplete(it.boardState.id!!, timer.elapsed()) },
    )

    val uiState: StateFlow<FailedPuzzlesUiState> = playSession.state
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FailedPuzzlesUiState.Loading,
        )

    init {
        timer.start()
        viewModelScope.launch(dispatcher) { playSession.run() }
    }

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    fun onSquareClicked(selection: Locus) = playSession.accept(PlayIntent.SelectSquare(selection))
    fun onPromote(to: Piece) = playSession.accept(PlayIntent.Promote(to))
    fun onHintRequested() = playSession.accept(PlayIntent.Hint)
    fun onDismissCompletion() = playSession.clearSummary()
    fun onAnalyzeFailedPuzzle(puzzleId: Int) = navDispatcher.navigate(NavigationDispatcher.Destination.Analysis(puzzleId))

    private fun PlaySessionState.toUiState(): FailedPuzzlesUiState = when (status) {
        PlaySessionState.Status.Failed -> FailedPuzzlesUiState.Failed
        PlaySessionState.Status.Loading -> FailedPuzzlesUiState.Loading
        PlaySessionState.Status.Ready,
        PlaySessionState.Status.Paused,
        PlaySessionState.Status.Playing -> FailedPuzzlesUiState.Playing(
            data = boardState,
            progress = FailedPuzzlesUiState.Progress(
                solved = results.count { it.success },
                total = results.size,
            ),
            results = results,
        )
        PlaySessionState.Status.Ended -> if (results.isEmpty()) {
            FailedPuzzlesUiState.Empty
        } else {
            FailedPuzzlesUiState.Finished(
                data = boardState,
                progress = FailedPuzzlesUiState.Progress(
                    solved = results.count { it.success },
                    total = results.size,
                ),
                results = results,
                showCompletionDialog = showSummary,
            )
        }
    }
}
