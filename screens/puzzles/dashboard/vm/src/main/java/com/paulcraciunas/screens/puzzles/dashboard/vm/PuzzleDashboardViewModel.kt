package com.paulcraciunas.screens.puzzles.dashboard.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PuzzleDashboardViewModel @Inject constructor(
    userRepository: UserRepository,
) : ViewModel() {
    val uiState: StateFlow<PuzzleDashboardUiState> = userRepository.userUpdates()
        .map { user ->
            PuzzleDashboardUiState(
                userRating = user.ratings.current,
                failedPuzzlesCount = user.failedPuzzles.size,
                currentStreakCount = user.ratings.puzzleStreak.currentCount,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PuzzleDashboardUiState()
        )

    fun onPuzzleModeSelected(mode: PuzzleMode, onNavigate: (PuzzleMode) -> Unit) {
        // For now, just trigger navigation
        // In the future, this might trigger analytics, state updates, etc.
        onNavigate(mode)
    }
}
