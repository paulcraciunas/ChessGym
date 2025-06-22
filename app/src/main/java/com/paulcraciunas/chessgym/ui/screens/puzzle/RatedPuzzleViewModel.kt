package com.paulcraciunas.chessgym.ui.screens.puzzle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.puzzles.api.usecases.GetPuzzleByRating
import com.paulcraciunas.settings.user.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

data class RatedPuzzleUiState(
    val puzzle: Puzzle? = null,
    val isLoading: Boolean = true,
    val showHint: Boolean = false,
    val moves: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val wasSuccessful: Boolean = false,
    val ratingChange: Int = 0,
    val newRating: Int = 0
)

@HiltViewModel
class RatedPuzzleViewModel @Inject constructor(
    private val getPuzzleByRating: GetPuzzleByRating,
    private val userStatsRepository: UserStatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RatedPuzzleUiState())
    val uiState: StateFlow<RatedPuzzleUiState> = _uiState.asStateFlow()

    init {
        loadPuzzle()
    }

    private fun loadPuzzle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val userStats = userStatsRepository.userStats.first()
            val targetRating = userStats.currentRating

            val puzzle = getPuzzleByRating(targetRating)

            puzzle.start() // TODO Paul: Make the first move
            _uiState.value = _uiState.value.copy(
                puzzle = puzzle,
                isLoading = false,
                moves = extractMoves(puzzle)
            )
        }
    }

    fun onSquareClicked(at: Locus) {
        val puzzle = _uiState.value.puzzle ?: return
        if (puzzle.state != Puzzle.State.InProgress) return

        // TODO Paul: update this. We need a UI abstraction here
//        puzzle.play(from, to)

        // Update the move list
        val newMoves = _uiState.value.moves.toMutableList()
        // Add the move in algebraic notation if possible
        _uiState.value = _uiState.value.copy(
            moves = newMoves
        )

        // Check if puzzle is completed
        when (puzzle.state) {
            Puzzle.State.Success -> {
                handlePuzzleCompletion(true)
            }

            Puzzle.State.Failed -> {
                handlePuzzleCompletion(false)
            }

            else -> {
                // Continue playing
            }
        }
    }

    fun requestHint() {
        // TODO Paul: select the correct piece
    }

    fun abandonPuzzle() {
        val puzzle = _uiState.value.puzzle ?: return
        puzzle.abandon()
        handlePuzzleCompletion(false)
    }

    fun playAnotherPuzzle() {
        _uiState.value = RatedPuzzleUiState()
        loadPuzzle()
    }

    private fun handlePuzzleCompletion(wasSuccessful: Boolean) {
        viewModelScope.launch {
            // TODO Paul: all these calls to the userStats should be encapsulated in a use case
            val userStats = userStatsRepository.userStats.first()
            val puzzle = _uiState.value.puzzle ?: return@launch

            // Update puzzles played count
            userStatsRepository.updatePuzzlesPlayed(userStats.puzzlesPlayed + 1)

            if (wasSuccessful) {
                // Update puzzles solved count
                userStatsRepository.updatePuzzlesSolved(userStats.puzzlesSolved + 1)
            }

            // TODO Paul: this should be a use case
            // Calculate ELO rating change
            val ratingChange = calculateEloChange(
                playerRating = userStats.currentRating,
                puzzleRating = puzzle.rating,
                wasSuccessful = wasSuccessful
            )

            val newRating = userStats.currentRating + ratingChange
            userStatsRepository.updateCurrentRating(newRating)

            _uiState.value = _uiState.value.copy(
                isCompleted = true,
                wasSuccessful = wasSuccessful,
                ratingChange = ratingChange,
                newRating = newRating,
            )
        }
    }

    private fun calculateEloChange(
        playerRating: Int,
        puzzleRating: Int,
        wasSuccessful: Boolean
    ): Int {
        val k = 32 // K-factor for rating changes
        val expectedScore = 1.0 / (1.0 + Math.pow(10.0, (puzzleRating - playerRating) / 400.0))
        val actualScore = if (wasSuccessful) 1.0 else 0.0
        return round(k * (actualScore - expectedScore)).toInt()
    }

    private fun extractMoves(puzzle: Puzzle): List<String> {
        // This would extract the move list from the puzzle
        // For now, return empty list - would need to implement based on puzzle structure
        return emptyList()
    }
}
