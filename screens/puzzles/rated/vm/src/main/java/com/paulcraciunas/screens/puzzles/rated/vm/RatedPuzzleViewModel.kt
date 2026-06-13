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
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.screens.data.PuzzleSolution
import com.paulcraciunas.screens.data.ScriptedOpponent
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.SinglePlaySession
import com.paulcraciunas.screens.data.engine.SingleSessionState
import com.paulcraciunas.screens.data.engine.toSoundEvents
import com.paulcraciunas.screens.data.utils.SequentialJob
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class RatedPuzzleViewModel @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val timer: Timer,
    private val getRatedPuzzle: GetRatedPuzzle,
    private val onPuzzleComplete: OnPuzzleComplete,
    sounds: SoundCoordinator,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val ratingChange = MutableStateFlow(EloResult(0, 0))
    private val playSession = SinglePlaySession(
        settingsRepository = appSettingsRepository,
        config = ratedSessionConfiguration(),
    )
    private val runJob = SequentialJob(viewModelScope)

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
        loadNextPuzzle()
        playSession.state
            .toSoundEvents()
            .filter { it is SoundCoordinator.SoundEvent.Move } // Don't want start-game and end-game sounds here
            .onEach { sounds.trigger(it) }
            .launchIn(viewModelScope)
    }

    fun onStop() { timer.pause() }
    fun onStart() { timer.resume() }
    fun onSquareClicked(selection: Locus) = playSession.accept(PlayIntent.SelectSquare(selection))
    fun onPromote(to: Piece) = playSession.accept(PlayIntent.Promote(to))
    fun onHintRequested() = playSession.accept(PlayIntent.Hint)
    fun onAbandon() = playSession.accept(PlayIntent.RequestAbandon)
    fun onAbandonDismissed() = playSession.accept(PlayIntent.DismissAbandon)
    fun onAbandonConfirmed() = playSession.accept(PlayIntent.ConfirmAbandon)

    fun onNextPuzzle() {
        playSession.reset()
        loadNextPuzzle()
    }

    fun onNavigateBackPressed(): Boolean {
        val isPlaying = playSession.state.value.status == SingleSessionState.Status.Playing
        if (isPlaying) {
            playSession.accept(PlayIntent.RequestAbandon)
        }
        return isPlaying
    }

    private fun loadNextPuzzle() {
        runJob.launch(defaultDispatcher) {
            try {
                val puzzleData = getRatedPuzzle()
                ratingChange.value = puzzleData.ratingChange
                val session = BoardSession(
                    navigation = NoOpNavigation,
                    solution = PuzzleSolution(puzzleData.puzzle),
                    opponent = ScriptedOpponent(puzzleData.puzzle),
                ).load(PuzzlePlayableBoard(puzzleData.puzzle))
                playSession.run(session)
                reportPuzzleComplete()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                playSession.reportError(e.message)
            }
        }
    }

    private suspend fun reportPuzzleComplete() {
        val finalState = playSession.state.value
        onPuzzleComplete(
            PuzzleCompletionResult(
                puzzleId = finalState.boardState.id,
                puzzleRating = finalState.boardState.rating!!,
                wasSuccessful = finalState.boardState.won,
                ratingChange = ratingChange.value.getNormalized(success = finalState.boardState.won),
                timeSpentMillis = timer.elapsed(),
            )
        )
    }

    private fun SingleSessionState.toUiState(elo: EloResult): RatedPuzzleUiState = when (status) {
        SingleSessionState.Status.Failed -> RatedPuzzleUiState.Failed
        SingleSessionState.Status.Loading -> RatedPuzzleUiState.Loading
        SingleSessionState.Status.GameOver -> RatedPuzzleUiState.Finished(
            rating = boardState.rating ?: 0,
            data = boardState,
            success = boardState.won,
            ratingChange = elo.get(success = boardState.won),
        )
        SingleSessionState.Status.Ready,
        SingleSessionState.Status.Playing -> RatedPuzzleUiState.Playing(
            rating = boardState.rating ?: 0,
            data = boardState,
            hintEnabled = hintAvailable,
            showAbandonDialog = abandonRequested,
        )
    }
}
