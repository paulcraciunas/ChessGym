package com.paulcraciunas.chessgym.debug

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.NoOpSolution
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.screens.data.ScriptedOpponent
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.PlaySession
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration
import com.paulcraciunas.screens.data.engine.PlaySessionState
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.utils.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DebugPuzzleViewModel @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    appSettingsRepository: AppSettingsRepository,
    puzzleRepository: PuzzleRepository,
) : ViewModel() {
    private val puzzleId = MutableStateFlow(0)
    private val playSession = PlaySession(
        settingsRepository = appSettingsRepository,
        config = PlaySessionConfiguration(),
        sessions = {
            puzzleId.map { id ->
                puzzleRepository.getById(id)?.let {
                    BoardSession(
                        navigation = NoOpNavigation,
                        solution = NoOpSolution,
                        opponent = ScriptedOpponent(it),
                    ).load(PuzzlePlayableBoard(it))
                } ?: throw Exception("Puzzle #$id not found")
            }
        }
    )

    val uiState: StateFlow<DebugPuzzleUiState> = playSession.state
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DebugPuzzleUiState.Idle
        )

    private var runJob: Job? = null

    fun loadPuzzle(id: Int) {
        puzzleId.update { id }
        runJob?.cancel()
        runJob = viewModelScope.launch(dispatcher) { playSession.run() }
    }

    fun onSquareClicked(selection: Locus) = playSession.accept(PlayIntent.SelectSquare(selection = selection))
    fun onPromote(to: Piece) = playSession.accept(PlayIntent.Promote(to = to))
}

private fun PlaySessionState.toUiState(): DebugPuzzleUiState = when (status) {
    PlaySessionState.Status.Failed -> DebugPuzzleUiState.Error(message = errorMessage ?: "")
    PlaySessionState.Status.Loading -> DebugPuzzleUiState.Loading
    PlaySessionState.Status.Ended -> DebugPuzzleUiState.Finished(data = boardState, isSuccess = results.last().success)
    else -> DebugPuzzleUiState.Playing(data = boardState)
}

sealed class DebugPuzzleUiState {
    data object Idle : DebugPuzzleUiState()
    data object Loading : DebugPuzzleUiState()
    data class Error(val message: String) : DebugPuzzleUiState()

    @Immutable
    abstract class WithBoard : DebugPuzzleUiState() {
        abstract val data: BoardState
    }

    @Immutable
    data class Playing(override val data: BoardState) : WithBoard()

    @Immutable
    data class Finished(override val data: BoardState, val isSuccess: Boolean) : WithBoard()
}
