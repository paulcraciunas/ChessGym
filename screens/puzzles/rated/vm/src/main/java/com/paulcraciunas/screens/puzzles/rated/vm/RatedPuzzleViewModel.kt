package com.paulcraciunas.screens.puzzles.rated.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.impl.board.BoardFactory
import com.paulcraciunas.screens.common.model.BoardViewDataBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatedPuzzleViewModel @Inject constructor(
    // TODO: Inject actual repositories when ready
) : ViewModel() {

    private val _uiState = MutableStateFlow<RatedPuzzleUiState>(RatedPuzzleUiState.Loading)
    val uiState: StateFlow<RatedPuzzleUiState> = _uiState.asStateFlow()

    init {
        loadPuzzle()
    }

    private fun loadPuzzle() {
        viewModelScope.launch {
            _uiState.value = RatedPuzzleUiState.Loading
            
            // TODO: Replace with actual puzzle fetching logic
            val dummyPuzzleData = createDummyPuzzleData()
            
            _uiState.value = RatedPuzzleUiState.Playing(
                data = dummyPuzzleData,
                hintEnabled = true,
                showAbandonDialog = false
            )
        }
    }

    fun onSquareClicked(rank: Rank, file: File) {
        val currentState = _uiState.value
        if (currentState !is RatedPuzzleUiState.Playing) return
        
        val locus = Locus(file, rank)
        // TODO: Implement actual move logic
        println("Square clicked: $locus")
        
        // Simulate puzzle completion (randomly for demo)
        val random = (0..100).random()
        when {
            random < 70 -> {
                // Simulate successful puzzle solve
                _uiState.value = RatedPuzzleUiState.Finished(
                    data = currentState.data,
                    success = true,
                    ratingChange = calculateRatingGain(currentState.data.rating)
                )
            }
            random < 90 -> {
                // Continue playing - no state change needed for now
                // TODO: Update board position when actual move logic is implemented
                println("Continue playing")
            }
            else -> {
                // Simulate puzzle failure
                _uiState.value = RatedPuzzleUiState.Finished(
                    data = currentState.data,
                    success = false,
                    ratingChange = calculateRatingLoss(currentState.data.rating)
                )
            }
        }
    }
    
    private fun calculateRatingGain(puzzleRating: Int): Int {
        // Dummy logic for rating gain - in reality this would be more complex
        return when {
            puzzleRating > 1600 -> 12
            puzzleRating > 1400 -> 15
            else -> 18
        }
    }
    
    private fun calculateRatingLoss(puzzleRating: Int): Int {
        // Dummy logic for rating loss - in reality this would be more complex
        return when {
            puzzleRating > 1600 -> -8
            puzzleRating > 1400 -> -12
            else -> -15
        }
    }

    fun onHintRequested() {
        val currentState = _uiState.value
        if (currentState !is RatedPuzzleUiState.Playing) return
        
        // TODO: Implement actual hint logic
        _uiState.value = currentState.copy(hintEnabled = false)
        // For now, just disable the hint button after use
        // TODO: Show actual hint UI when hint system is implemented
        println("Hint requested - not yet implemented")
    }

    fun onAbandonRequested() {
        val currentState = _uiState.value
        if (currentState is RatedPuzzleUiState.Playing) {
            _uiState.value = currentState.copy(showAbandonDialog = true)
        }
    }

    fun onAbandonConfirmed() {
        val currentState = _uiState.value
        if (currentState is RatedPuzzleUiState.Playing) {
            _uiState.value = RatedPuzzleUiState.Finished(
                data = currentState.data,
                success = false,
                ratingChange = -10 // Dummy rating loss for abandoning
            )
        }
    }

    fun onAbandonCancelled() {
        val currentState = _uiState.value
        if (currentState is RatedPuzzleUiState.Playing) {
            _uiState.value = currentState.copy(showAbandonDialog = false)
        }
    }

    fun onNavigateBackPressed() {
        when (_uiState.value) {
            is RatedPuzzleUiState.Playing -> {
                // Show abandon dialog
                onAbandonRequested()
            }
            else -> {
                // Navigation is handled externally
                // This method is kept for compatibility but actual navigation
                // is handled by the composable's onNavigateBack parameter
            }
        }
    }

    fun onNextPuzzle() {
        // Reset state and load next puzzle
        loadPuzzle()
    }

    fun onNavigateToStart() {
        // TODO: Navigate to the starting position of the puzzle
        println("Navigate to start")
    }

    fun onNavigateBackMove() {
        // TODO: Navigate to previous move in solution
        println("Navigate back move")
    }

    fun onNavigateNextMove() {
        // TODO: Navigate to next move in solution
        println("Navigate next move")
    }

    fun onNavigateToEnd() {
        // TODO: Navigate to the final position of the puzzle
        println("Navigate to end")
    }

    private fun createDummyPuzzleData(): RatedPuzzleUiState.PuzzleData {
        // Create a dummy puzzle with a standard chess position
        val board = BoardFactory.defaultBoard()
        val boardViewData = BoardViewDataBuilder().apply {
            loadBoard(board)
            // TODO: Add highlighting for last move or hints
        }.build()
        
        return RatedPuzzleUiState.PuzzleData(
            rating = 1450,
            player = Side.WHITE,
            boardData = boardViewData,
            captured = mapOf(
                Side.WHITE to listOf(Piece.Pawn, Piece.Knight, Piece.Pawn), // Dummy captured pieces
                Side.BLACK to listOf(Piece.Bishop, Piece.Pawn, Piece.Pawn, Piece.Rook) // Dummy captured pieces
            )
        )
    }
}
