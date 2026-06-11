package com.paulcraciunas.screens.boardvis.pieces.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.boardvis.GenerateKnightPathExercise
import com.paulcraciunas.domain.api.boardvis.KnightPathExercise
import com.paulcraciunas.domain.api.boardvis.KnightPathResult
import com.paulcraciunas.domain.api.boardvis.OnKnightPathComplete
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.screens.data.BoardViewData
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
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class KnightPathViewModel @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val generateExercise: GenerateKnightPathExercise,
    private val onKnightPathComplete: OnKnightPathComplete,
    private val countdownTimer: CountdownTimer,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    private val gameJob = SequentialJob(viewModelScope)

    val uiState: StateFlow<KnightPathUiState> = _gameState
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = KnightPathUiState.Setup
        )

    fun onPlayClicked() {
        if (_gameState.value.status != GameState.Status.Setup) return
        gameJob.launch(dispatcher) {
            val exercise = generateExercise(INITIAL_MOVES_REQUIRED)
            _gameState.update {
                GameState(
                    status = GameState.Status.Playing,
                    exercise = exercise,
                    pathIndex = 0,
                    score = 0,
                    exercisesAtCurrentDifficulty = 0,
                    currentDifficulty = INITIAL_MOVES_REQUIRED,
                    previousHighScore = userRepository.get().highScores.knightPath,
                    timeRemaining = KnightPathUiState.DEFAULT_DURATION_SECONDS.asRemainder(),
                )
            }
            runTimer()
        }
    }

    fun onSquareClicked(locus: Locus) {
        val state = _gameState.value
        if (state.status != GameState.Status.Playing) return
        val exercise = state.exercise ?: return

        val expectedNext = exercise.path[state.pathIndex + 1]
        when {
            locus == expectedNext -> _gameState.update { advanceToNextStep(it, it.exercise!!) }
            !exercise.board.isEmpty(locus) -> return
            else -> onWrongMove()
        }
    }

    fun onPlayAgain() {
        gameJob.cancel()
        _gameState.update { GameState() }
    }

    private fun advanceToNextStep(state: GameState, exercise: KnightPathExercise): GameState {
        val currentPos = exercise.path[state.pathIndex]
        val newPathIndex = state.pathIndex + 1
        val nextPos = exercise.path[newPathIndex]

        exercise.board.move(from = currentPos, to = nextPos, turn = Side.WHITE)

        if (nextPos == exercise.destination) {
            val newScore = state.score + 1
            val newExercisesAtDifficulty = state.exercisesAtCurrentDifficulty + 1
            val newDifficulty = nextDifficulty(state.currentDifficulty, newExercisesAtDifficulty)
            val adjustedCount = if (newDifficulty != state.currentDifficulty) 0 else newExercisesAtDifficulty

            val nextExercise = generateExercise(newDifficulty)

            return state.copy(
                exercise = nextExercise,
                pathIndex = 0,
                score = newScore,
                currentDifficulty = newDifficulty,
                exercisesAtCurrentDifficulty = adjustedCount,
            )
        }
        return state.copy(pathIndex = newPathIndex)
    }

    private fun onWrongMove() {
        gameJob.cancel()
        viewModelScope.launch(dispatcher) {
            onGameOver(wasWrongMove = true)
        }
    }

    private suspend fun runTimer() {
        countdownTimer.start(durationMs = DURATION_MS, intervalMillis = INTERVAL_MS).collect { remainder ->
            _gameState.update { it.copy(timeRemaining = remainder) }
        }
        onGameOver(wasWrongMove = false)
    }

    private suspend fun onGameOver(wasWrongMove: Boolean) {
        val timeSpent = DURATION_MS - _gameState.value.timeRemaining.ms
        _gameState.update {
            it.copy(
                status = GameState.Status.Finished,
                wasWrongMove = wasWrongMove,
                isNewHighScore = it.score > it.previousHighScore,
            )
        }
        onKnightPathComplete(
            KnightPathResult(
                score = _gameState.value.score,
                timeSpentMillis = timeSpent
            )
        )
    }

    private fun nextDifficulty(current: Int, exercisesCompleted: Int): Int {
        if (exercisesCompleted >= EXERCISES_PER_DIFFICULTY && current < MAX_MOVES) {
            return current + 1
        }
        return current
    }

    private data class GameState(
        val status: Status = Status.Setup,
        val exercise: KnightPathExercise? = null,
        val pathIndex: Int = 0,
        val score: Int = 0,
        val exercisesAtCurrentDifficulty: Int = 0,
        val currentDifficulty: Int = INITIAL_MOVES_REQUIRED,
        val wasWrongMove: Boolean = false,
        val isNewHighScore: Boolean = false,
        val previousHighScore: Int = 0,
        val timeRemaining: CountdownTimer.Remainder = CountdownTimer.Remainder(
            seconds = KnightPathUiState.DEFAULT_DURATION_SECONDS,
            millis = 0
        ),
    ) {
        enum class Status { Setup, Playing, Finished }

        fun toUiState(): KnightPathUiState = when (status) {
            Status.Setup -> KnightPathUiState.Setup
            Status.Playing -> KnightPathUiState.Playing(
                boardData = BoardViewData.from(board = exercise!!.board),
                destination = exercise.destination,
                score = score,
                timeRemaining = timeRemaining.format(),
            )
            Status.Finished -> KnightPathUiState.GameOver(
                boardData = BoardViewData.from(board = exercise!!.board),
                timeRemaining = timeRemaining.format(),
                score = score,
                isNewHighScore = isNewHighScore,
                wasWrongMove = wasWrongMove,
            )
        }
    }

    companion object {
        private const val INITIAL_MOVES_REQUIRED = 2
        private const val EXERCISES_PER_DIFFICULTY = 3
        private const val MAX_MOVES = 6
        private const val INTERVAL_MS = 100L
        private const val DURATION_MS = KnightPathUiState.DEFAULT_DURATION_SECONDS * 1000L
    }
}

private fun Int.asRemainder() = CountdownTimer.Remainder(seconds = this, millis = 0)

private fun CountdownTimer.Remainder.format(): String =
    String.format(Locale.getDefault(), "%d.%d", seconds, millis / 100)
