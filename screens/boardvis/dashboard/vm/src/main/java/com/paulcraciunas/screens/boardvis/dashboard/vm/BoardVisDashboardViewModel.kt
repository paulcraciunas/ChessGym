package com.paulcraciunas.screens.boardvis.dashboard.vm

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
class BoardVisDashboardViewModel @Inject constructor(
    userRepository: UserRepository
) : ViewModel() {

    val uiState: StateFlow<BoardVisDashboardUiState> = userRepository.userUpdates()
        .map { user ->
            BoardVisDashboardUiState(
                findSquareHighScore = user.highScores.findTheSquare,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BoardVisDashboardUiState()
        )

    fun onModeSelected(mode: BoardVisMode, onNavigate: (BoardVisMode) -> Unit) {
        onNavigate(mode)
    }
}
