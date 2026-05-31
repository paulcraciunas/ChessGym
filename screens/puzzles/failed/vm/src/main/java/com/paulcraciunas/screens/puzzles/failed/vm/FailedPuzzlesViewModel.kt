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
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.PuzzleResult
import com.paulcraciunas.screens.data.PuzzleSessionFactory
import com.paulcraciunas.screens.data.SessionSettings
import com.paulcraciunas.screens.data.runAs
import com.paulcraciunas.screens.data.updateAs
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
    appSettingsRepository: AppSettingsRepository,
    private val timer: Timer,
) : ViewModel() {
    private val factory = PuzzleSessionFactory(withSolution = false)
    private val session = factory.get()
    private val _navigateToAnalysis = Channel<PuzzleAnalysisData>(Channel.BUFFERED)
    val navigateToAnalysis: Flow<PuzzleAnalysisData> = _navigateToAnalysis.receiveAsFlow()

    private val _uiState = MutableStateFlow<FailedPuzzlesUiState>(FailedPuzzlesUiState.Loading)
    val uiState: StateFlow<FailedPuzzlesUiState> = _uiState.asStateFlow()

    init {
        session.bindSettings(viewModelScope, appSettingsRepository.appSettings.map {
            SessionSettings(it.autoPromote, it.enableAnimations)
        })
        observeBoardState()
        loadPuzzles()
    }

    private fun observeBoardState() {
        viewModelScope.launch {
            session.data.collect { boardState ->
                _uiState.updateAs { it: FailedPuzzlesUiState.Playing -> it.copy(data = boardState) }
                _uiState.runAs<FailedPuzzlesUiState.Playing> {
                    if (boardState.isOver && boardState.interactive) onPuzzleCompleted(boardState, boardState.won)
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

    private fun loadPuzzles() {
        viewModelScope.launch {
            try {
                getFailedPuzzles.load(viewModelScope)
                val puzzle = getFailedPuzzles.next()
                if (puzzle == null) {
                    _uiState.update { FailedPuzzlesUiState.Empty }
                } else {
                    factory.load(viewModelScope, puzzle)
                    _uiState.update {
                        FailedPuzzlesUiState.Playing(
                            data = session.currentState,
                            progress = FailedPuzzlesUiState.Progress(solved = 0, total = getFailedPuzzles.totalCount()),
                            results = emptyList(),
                        )
                    }
                    timer.start()
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load failed puzzles")
                _uiState.update { FailedPuzzlesUiState.Failed }
            }
        }
    }

    fun onSquareClicked(selection: Locus) = _uiState.runAs<FailedPuzzlesUiState.Playing> { session.onClick(selection) }
    fun onPromote(to: Piece) = session.promoteIfPending(to)
    fun onDismissCompletion() = _uiState.updateAs { it: FailedPuzzlesUiState.Finished -> it.copy(showCompletionDialog = false) }
    fun onAnalyzeFailedPuzzle(puzzleId: Int) {
        viewModelScope.launch {
            getPuzzleFen(puzzleId)?.let { data -> _navigateToAnalysis.send(data) }
                ?: Timber.w("Failed to get puzzle fen")
        }
    }

    private fun onPuzzleCompleted(data: BoardState, isSuccess: Boolean) = _uiState.runAs<FailedPuzzlesUiState.Playing> { state ->
        viewModelScope.launch {
            if (isSuccess) {
                data.id?.let { onFailedPuzzleComplete(it, timer.elapsed()) }
            }
            val updatedResults = state.results + PuzzleResult(id = data.id, rating = data.rating!!, success = isSuccess)
            val progress = state.progress.copy(solved = updatedResults.count { it.success })
            val nextPuzzle = getFailedPuzzles.next()
            if (nextPuzzle != null) {
                factory.load(viewModelScope, nextPuzzle)
                _uiState.update {
                    FailedPuzzlesUiState.Playing(
                        data = session.currentState,
                        progress = progress,
                        results = updatedResults,
                    )
                }
                timer.start()
            } else {
                _uiState.update {
                    FailedPuzzlesUiState.Finished(
                        data = data,
                        progress = progress,
                        results = updatedResults,
                        showCompletionDialog = true,
                    )
                }
            }
        }
    }
}
