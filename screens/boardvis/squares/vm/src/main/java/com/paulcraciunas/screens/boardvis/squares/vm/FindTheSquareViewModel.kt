package com.paulcraciunas.screens.boardvis.squares.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.GenerateRandomLoci
import com.paulcraciunas.domain.api.boardvis.FindSquareResult
import com.paulcraciunas.domain.api.boardvis.OnFindSquareComplete
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.DefaultTimer
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.screens.data.runAs
import com.paulcraciunas.screens.data.toSide
import com.paulcraciunas.screens.data.updateAs
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindTheSquareViewModel @Inject constructor(
    private val generateRandomLoci: GenerateRandomLoci,
    private val onFindSquareComplete: OnFindSquareComplete,
    @param:DefaultTimer private val countdownTimer: CountdownTimer,
    private val userRepository: UserRepository,
    private val randomFactory: RandomFactory,
    gameDuration: GameDuration,
) : ViewModel() {
    private val durationSeconds: Int = gameDuration.seconds
    private val _gameState = MutableStateFlow<GameState>(GameState.Setup())
    val uiState: StateFlow<FindTheSquareUiState> = combine(
        _gameState,
        countdownTimer.remaining.map { it.roundSeconds() }
    ) { gameState, remainingSeconds ->
        gameState.toUiState(remainingSeconds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FindTheSquareUiState.Setup()
    )

    private var timerObserverJob: Job? = null
    private var currentHighScore: Int = 0

    init {
        viewModelScope.launch {
            userRepository.userUpdates().map { it.highScores.findTheSquare }.collect { currentHighScore = it }
        }
        countdownTimer.set(durationSeconds)
    }

    private fun observeTimer() {
        timerObserverJob?.cancel()
        timerObserverJob = viewModelScope.launch {
            countdownTimer.remaining.first { it.roundSeconds() <= 0 }
            _gameState.runAs<GameState.Playing> { finishGame(it) }
        }
    }

    fun onSideSelected(side: SideSelection) = _gameState.updateAs { it: GameState.Setup -> it.copy(selectedSide = side) }
    fun onPlayClicked() = _gameState.runAs<GameState.Setup> { state ->
        countdownTimer.start(scope = viewModelScope)
        observeTimer()
        _gameState.update {
            GameState.Playing(
                side = state.selectedSide.toSide { randomFactory.nextInt(0, 2) },
                currentSquare = generateRandomLoci(),
                score = 0,
                showError = false
            )
        }
    }

    fun onSquareClicked(locus: Locus) = _gameState.updateAs { it: GameState.Playing ->
        if (locus == it.currentSquare) { // Correct answer
            it.copy(currentSquare = generateRandomLoci(), score = it.score + 1, showError = false)
        } else { // Wrong answer - show error
            it.copy(showError = true)
        }
    }

    fun onPlayAgain() {
        countdownTimer.set(durationSeconds)
        _gameState.update { GameState.Setup() }
    }

    fun onErrorShown() = _gameState.updateAs { it: GameState.Playing -> it.copy(showError = false) }

    private fun finishGame(playingState: GameState.Playing) {
        countdownTimer.stop()

        val score = playingState.score
        _gameState.update {
            GameState.GameOver(
                side = playingState.side,
                score = score,
                isNewHighScore = score > currentHighScore,
                previousHighScore = currentHighScore,
            )
        }
        viewModelScope.launch { // Log the result
            onFindSquareComplete(FindSquareResult(score = score, timeSpentMillis = countdownTimer.elapsedMillis()))
        }
    }

    // Internal state representation
    private sealed class GameState {
        abstract fun toUiState(remainingSeconds: Int): FindTheSquareUiState

        data class Setup(
            val selectedSide: SideSelection = SideSelection.WHITE,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = FindTheSquareUiState.Setup(
                selectedSide = selectedSide,
                timeRemainingSeconds = remainingSeconds
            )
        }

        data class Playing(
            val side: Side,
            val currentSquare: Locus,
            val score: Int,
            val showError: Boolean,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = FindTheSquareUiState.Playing(
                orientation = side,
                currentSquare = currentSquare,
                score = score,
                timeRemainingSeconds = remainingSeconds,
                showError = showError
            )
        }

        data class GameOver(
            val side: Side,
            val score: Int,
            val isNewHighScore: Boolean,
            val previousHighScore: Int,
        ) : GameState() {
            override fun toUiState(remainingSeconds: Int) = FindTheSquareUiState.GameOver(
                orientation = side,
                score = score,
                isNewHighScore = isNewHighScore,
                previousHighScore = previousHighScore,
            )
        }
    }
}
