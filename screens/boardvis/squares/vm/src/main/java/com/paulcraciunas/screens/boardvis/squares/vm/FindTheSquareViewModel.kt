package com.paulcraciunas.screens.boardvis.squares.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.boardvis.FindSquareResult
import com.paulcraciunas.domain.api.GenerateRandomLoci
import com.paulcraciunas.domain.api.boardvis.OnFindSquareComplete
import com.paulcraciunas.domain.api.general.DefaultTimer
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.common.controls.SideSelection
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
        // Handle time expiry during playing
        val state = if (gameState is GameState.Playing && remainingSeconds <= 0) {
            _gameState.value = finishGame(gameState)
            _gameState.value
        } else gameState
        state.toUiState(remainingSeconds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FindTheSquareUiState.Setup()
    )

    private var currentHighScore: Int = 0

    init {
        viewModelScope.launch {
            userRepository.userUpdates().map { it.highScores.findTheSquare }.collect { currentHighScore = it }
        }
        countdownTimer.set(durationSeconds)
    }

    fun onSideSelected(side: SideSelection) {
        val currentState = _gameState.value
        if (currentState is GameState.Setup) {
            _gameState.value = currentState.copy(selectedSide = side)
        }
    }

    fun onPlayClicked() {
        val currentState = _gameState.value
        if (currentState !is GameState.Setup) return

        val side = when (currentState.selectedSide) {
            SideSelection.WHITE -> Side.WHITE
            SideSelection.BLACK -> Side.BLACK
            SideSelection.RANDOM -> Side.fromCode(randomFactory.nextInt(0, 2))
        }

        countdownTimer.start(scope = viewModelScope)
        _gameState.value = GameState.Playing(
            side = side,
            currentSquare = generateRandomLoci(),
            score = 0,
            showError = false
        )
    }

    fun onSquareClicked(locus: Locus) {
        val currentState = _gameState.value
        if (currentState !is GameState.Playing) return

        if (locus == currentState.currentSquare) {
            // Correct answer
            _gameState.value = currentState.copy(
                currentSquare = generateRandomLoci(),
                score = currentState.score + 1,
                showError = false
            )
        } else {
            // Wrong answer - show error
            _gameState.value = currentState.copy(showError = true)
        }
    }

    fun onPlayAgain() {
        countdownTimer.set(durationSeconds)
        _gameState.value = GameState.Setup()
    }

    fun onErrorShown() {
        val currentState = _gameState.value
        if (currentState is GameState.Playing) {
            _gameState.value = currentState.copy(showError = false)
        }
    }

    private fun finishGame(playingState: GameState.Playing): GameState {
        countdownTimer.stop()

        val score = playingState.score
        val isNewHighScore = score > currentHighScore

        // Log the result
        viewModelScope.launch {
            onFindSquareComplete(
                FindSquareResult(
                    score = score,
                    timeSpentMillis = countdownTimer.elapsedMillis()
                )
            )
        }

        return GameState.GameOver(
            side = playingState.side,
            score = score,
            isNewHighScore = isNewHighScore,
            previousHighScore = currentHighScore,
        )
    }

    // Internal state representation
    private sealed class GameState {
        abstract fun toUiState(remainingSeconds: Int): FindTheSquareUiState

        data class Setup(
            val selectedSide: SideSelection = SideSelection.WHITE
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
            val showError: Boolean
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
