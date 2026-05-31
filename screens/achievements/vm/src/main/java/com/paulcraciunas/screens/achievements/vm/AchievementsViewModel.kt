package com.paulcraciunas.screens.achievements.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.achievements.GetAchievementState
import com.paulcraciunas.domain.api.achievements.MarkAchievementsSeen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val getAchievementState: GetAchievementState,
    private val markAchievementsSeen: MarkAchievementsSeen,
    private val stateAdapter: AchievementsUiStateAdapter,
) : ViewModel() {

    private val _selectedAchievement = MutableStateFlow<AchievementsUiState.AchievementState?>(null)

    val uiState: StateFlow<AchievementsUiState> = flow {
        val adaptedState = stateAdapter.adapt(getAchievementState())
        emit(adaptedState)
    }.catch { e ->
        Timber.w(e, "Error loading achievements")
        emit(AchievementsUiState(isLoading = false, isError = true))
    }.combine(_selectedAchievement) { state, selected ->
        state.copy(selectedAchievement = selected)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AchievementsUiState(isLoading = true)
    )

    fun onScreenVisible() {
        viewModelScope.launch {
            try {
                markAchievementsSeen()
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark achievements as seen")
            }
        }
    }

    fun onAchievementClicked(achievement: AchievementsUiState.AchievementState) {
        _selectedAchievement.value = achievement
    }

    fun onDismissDetail() {
        _selectedAchievement.value = null
    }
}
