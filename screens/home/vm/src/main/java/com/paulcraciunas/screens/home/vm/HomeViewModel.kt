package com.paulcraciunas.screens.home.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.settings.user.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stateAdapter: HomeUiStateAdapter,
    private val userStatsRepository: UserStatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userStatsRepository.userStats
                .collect { userStats ->
                    _uiState.value = HomeUiState(
                        userStats = stateAdapter.adapt(userStats),
                        isLoading = false
                    )
                }
        }
    }
}
