package com.paulcraciunas.chessgym.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.settings.user.UserStats
import com.paulcraciunas.settings.user.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userStats: UserStats = UserStats(
        puzzlesPlayed = 0,
        puzzlesSolved = 0,
        currentRating = 1200,
        bestRating = 1200,
        bestPuzzleRushScore = 0,
        bestBlindModeScore = 0,
        bestVisualizationScore = 0
    ),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userStatsRepository.userStats
                .collect { userStats ->
                    _uiState.value = HomeUiState(
                        userStats = userStats,
                        isLoading = false
                    )
                }
        }
    }
}
