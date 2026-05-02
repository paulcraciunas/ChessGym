package com.paulcraciunas.chessgym.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ForceAchievementViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForceAchievementUiState())
    val uiState: StateFlow<ForceAchievementUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    fun forceAchievement(achievement: Achievement, tier: Achievement.Tier) {
        viewModelScope.launch {
            try {
                val user = userRepository.get()
                val targetProgress = achievement.progressForTier(tier)
                val updatedProgress = user.achievements.progress.toMutableMap()
                updatedProgress[achievement.name] = targetProgress
                val updatedUser = user.copy(
                    achievements = user.achievements.copy(progress = updatedProgress)
                )
                userRepository.update(updatedUser)
                loadAchievements()
            } catch (e: Exception) {
                Timber.w(e, "Failed to force achievement %s to tier %s", achievement.name, tier.name)
            }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            try {
                val user = userRepository.get()
                val progress = user.achievements.progress
                val items = Achievement.entries.map { achievement ->
                    val progressValue = progress[achievement.name] ?: 0L
                    val currentTier = achievement.tierFrom(progressValue)
                    ForceAchievementUiState.AchievementItem(
                        achievement = achievement,
                        currentTier = currentTier,
                    )
                }
                _uiState.value = ForceAchievementUiState(
                    achievements = items,
                    isLoading = false,
                )
            } catch (e: Exception) {
                Timber.w(e, "Failed to load achievements")
                _uiState.value = ForceAchievementUiState(isLoading = false)
            }
        }
    }
}

private fun Achievement.progressForTier(tier: Achievement.Tier): Long {
    val previousTier = if (tier.ordinal > 0) {
        Achievement.Tier.entries[tier.ordinal - 1]
    } else {
        null
    }
    return nextTierProgress(previousTier)!!
}

data class ForceAchievementUiState(
    val achievements: List<AchievementItem> = emptyList(),
    val isLoading: Boolean = true,
) {
    data class AchievementItem(
        val achievement: Achievement,
        val currentTier: Achievement.Tier?,
    )
}
