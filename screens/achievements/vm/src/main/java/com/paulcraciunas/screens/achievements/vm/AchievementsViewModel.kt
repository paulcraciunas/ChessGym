package com.paulcraciunas.screens.achievements.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.achievements.GetAchievementState
import com.paulcraciunas.domain.api.achievements.MarkAchievementsSeen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val getAchievementState: GetAchievementState,
    private val markAchievementsSeen: MarkAchievementsSeen,
    private val stateAdapter: AchievementsUiStateAdapter,
) : ViewModel(), AchievementsInteractor {

    val uiState: StateFlow<AchievementsUiState> = flow {
        val adaptedState = stateAdapter.adapt(getAchievementState())
        emit(adaptedState)
    }.catch { e ->
        Timber.w(e, "Error loading achievements")
        emit(AchievementsUiState(isLoading = false, isError = true, achievements = emptyList()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AchievementsUiState(isLoading = true)
    )

    override fun onScreenVisible() {
        viewModelScope.launch {
            try {
                markAchievementsSeen()
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark achievements as seen")
            }
        }
    }
}
