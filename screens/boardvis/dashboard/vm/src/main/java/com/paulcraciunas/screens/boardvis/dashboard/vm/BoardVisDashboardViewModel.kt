package com.paulcraciunas.screens.boardvis.dashboard.vm

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
class BoardVisDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardVisDashboardUiState())
    val uiState: StateFlow<BoardVisDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.userUpdates().collect { user ->
                _uiState.value = _uiState.value.copy(
                    findSquareHighScore = user.highScores.findTheSquare,
                    isLoading = false
                )
            }
        }
    }

    fun onModeSelected(mode: BoardVisMode, onNavigate: (BoardVisMode) -> Unit) {
        onNavigate(mode)
    }
}
