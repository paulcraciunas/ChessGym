package com.paulcraciunas.screens.boardvis.squares.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.GenerateRandomLoci
import com.paulcraciunas.domain.api.boardvis.FindSquareResult
import com.paulcraciunas.domain.api.boardvis.OnFindSquareComplete
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.screens.data.RemainingTime
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.screens.data.toSide
import com.paulcraciunas.screens.data.utils.SequentialJob
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FindTheSquareViewModel @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val generateRandomLoci: GenerateRandomLoci,
    private val onFindSquareComplete: OnFindSquareComplete,
    private val countdownTimer: CountdownTimer,
    private val userRepository: UserRepository,
    private val randomFactory: RandomFactory,
    private val sounds: SoundCoordinator,
    gameDuration: GameDuration,
) : ViewModel() {
    private val durationSeconds: Int = gameDuration.seconds
    private val _gameState = MutableStateFlow<GameState>(GameState(timeRemaining = durationSeconds.asRemainder()))
    private val gameJob = SequentialJob(viewModelScope)

    val uiState: StateFlow<FindTheSquareUiState> = _gameState
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FindTheSquareUiState.Setup()
        )

    fun onSideSelected(side: SideSelection) = _gameState.update { it.copy(selectedSide = side) }
    fun onPlayClicked() {
        if (_gameState.value.status != GameState.Status.Setup) return
        gameJob.launch(dispatcher) {
            _gameState.update {
                it.copy(
                    status = GameState.Status.Playing,
                    orientation = it.selectedSide.toSide { randomFactory.nextInt(0, 2) },
                    currentSquare = generateRandomLoci(),
                    score = 0,
                    showError = false,
                    previousHighScore = userRepository.get().highScores.findTheSquare,
                )
            }
            startGame()
        }
    }

    fun onSquareClicked(locus: Locus) {
        val state = _gameState.value
        if (state.status != GameState.Status.Playing) return
        if (locus != state.currentSquare) {
            _gameState.update { it.copy(showError = true) } // Wrong answer
        } else {
            var newSquare = generateRandomLoci()
            while (newSquare == state.currentSquare) { // prevent the same square from showing up twice in a row
                newSquare = generateRandomLoci()
            }
            _gameState.update { it.copy(currentSquare = newSquare, score = it.score + 1, showError = false) } // Good job
        }
    }

    fun onPlayAgain() {
        gameJob.cancel()
        _gameState.update { GameState(timeRemaining = durationSeconds.asRemainder()) }
    }

    fun onErrorShown() {
        _gameState.update { if (it.status == GameState.Status.Playing) it.copy(showError = false) else it }
    }

    private suspend fun startGame() {
        val durationMs = _gameState.value.timeRemaining.seconds * 1000L
        var isTicking = false
        countdownTimer.start(durationMs = durationMs).collect { remainder ->
            _gameState.update { it.copy(timeRemaining = remainder) }
            if (remainder.seconds < FindTheSquareUiState.DANGER_DURATION_SECONDS && !isTicking) {
                isTicking = true
                sounds.trigger(SoundCoordinator.SoundEvent.Tick(FindTheSquareUiState.DANGER_DURATION_SECONDS))
            }
        }
        _gameState.update {
            it.copy(
                status = GameState.Status.Finished, // game ends when timer collection ends - i.e. time runs out
                isNewHighScore = it.score > it.previousHighScore,
            )
        }
        // Log the result
        onFindSquareComplete(FindSquareResult(score = _gameState.value.score, timeSpentMillis = durationMs))
    }

    private data class GameState(
        val timeRemaining: CountdownTimer.Remainder,
        val status: Status = Status.Setup,
        val selectedSide: SideSelection = SideSelection.WHITE,
        val orientation: Side = Side.WHITE,
        val currentSquare: Locus = Locus.a1,
        val score: Int = 0,
        val showError: Boolean = false,
        val isNewHighScore: Boolean = false,
        val previousHighScore: Int = 0,
    ) {
        enum class Status { Setup, Playing, Finished }

        fun toUiState(): FindTheSquareUiState = when (status) {
            Status.Setup -> FindTheSquareUiState.Setup(selectedSide = selectedSide)
            Status.Playing -> FindTheSquareUiState.Playing(
                orientation = orientation,
                currentSquare = currentSquare,
                score = score,
                timeRemaining = timeRemaining.toRemainingTime(),
                showError = showError,
            )
            Status.Finished -> FindTheSquareUiState.GameOver(
                orientation = orientation,
                score = score,
                isNewHighScore = isNewHighScore,
                previousHighScore = previousHighScore,
            )
        }
    }
}

internal fun CountdownTimer.Remainder.toRemainingTime(): RemainingTime = RemainingTime(
    value = format(),
    danger = this.seconds <= FindTheSquareUiState.DANGER_DURATION_SECONDS,
)

private fun Int.asRemainder() = CountdownTimer.Remainder(seconds = this, millis = 0)

private fun CountdownTimer.Remainder.format(): String =
    String.format(Locale.getDefault(), "%d.%d", seconds, millis / 100)
