package com.paulcraciunas.screens.puzzles.dashboard.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PuzzleDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PuzzleDashboardUiState())
    val uiState: StateFlow<PuzzleDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.userUpdates().collect { user ->
                _uiState.value = _uiState.value.copy(
                    userRating = user.ratings.current,
                    failedPuzzlesCount = user.failedPuzzles.size,
                    currentStreakCount = user.ratings.puzzleStreak.currentCount,
                    isLoading = false
                )
            }
        }
    }

    fun onPuzzleModeSelected(mode: PuzzleMode, onNavigate: (PuzzleMode) -> Unit) {
        // For now, just trigger navigation
        // In the future, this might trigger analytics, state updates, etc.
        onNavigate(mode)
    }
}
