package com.paulcraciunas.screens.tools.importgame.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.analysis.AnalyzeFullGame
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.screens.data.utils.SequentialJob
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerPgn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportGameViewModel @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @param:SerializerPgn private val pgnSerializer: Serializer,
    private val analysisUseCase: AnalyzeFullGame,
) : ViewModel() {
    private val adapter = ImportGameUiStateAdapter()
    private val analysisJob = SequentialJob(viewModelScope)
    private lateinit var game: Game

    private val _uiState = MutableStateFlow<ImportGameUiState>(ImportGameUiState.Setup())
    val uiState: StateFlow<ImportGameUiState> = _uiState.asStateFlow()

    fun onImport(pgn: String) {
        viewModelScope.launch(defaultDispatcher) {
            try {
                game = pgnSerializer.from(pgn)
                game.undoAll()

                _uiState.update {
                    adapter.adapt(game)
                }

                analysisJob.launch {
                    analysisUseCase.analyze(game).collect { progress ->
                        _uiState.update {
                            adapter.adapt(currentState = _uiState.value, progress = progress)
                        }
                    }
                }
            } catch (e: Throwable) {
                Timber.w(e, "Failed to import PGN")
                _uiState.update { ImportGameUiState.Setup(hasImportError = true) }
            }
        }
    }

    fun onNavigateBackPressed(): Boolean {
        _uiState.update {
            if (it is ImportGameUiState.Loading) it.copy(showAbandonDialog = true)
            else it
        }
        return _uiState.value is ImportGameUiState.Loading
    }

    fun onAbandonConfirmed() {
        analysisJob.cancel()
        _uiState.value = ImportGameUiState.Setup()
    }

    fun onAbandonDismissed() = _uiState.update {
        if (it is ImportGameUiState.Loading) it.copy(showAbandonDialog = false)
        else it
    }

    fun onFlipBoard() = _uiState.update {
        if (it is ImportGameUiState.Complete) it.copy(orientation = it.orientation.other())
        else it
    }

    fun onMoveSelected(moveIndex: Int) {
        val state = _uiState.value
        if (state !is ImportGameUiState.Complete) return

        game.undoAll()
        repeat(moveIndex) { game.replayNext() }

        _uiState.update {
            adapter.update(state, game)
        }
    }

    fun onJumpToStart() = navigate(game::canUndo, game::undoAll)
    fun onPreviousMove() = navigate(game::canUndo, game::undoLast)
    fun onNextMove() = navigate(game::canReplay, game::replayNext)
    fun onJumpToEnd() = navigate(game::canReplay, game::replayAll)

    override fun onCleared() {
        super.onCleared()
        analysisJob.cancel()
    }

    private fun navigate(guard: () -> Boolean, action: () -> Unit) {
        val state = _uiState.value
        if (state !is ImportGameUiState.Complete) return

        if (guard()) {
            action()
            _uiState.update {
                adapter.update(state, game)
            }
        }
    }
}
