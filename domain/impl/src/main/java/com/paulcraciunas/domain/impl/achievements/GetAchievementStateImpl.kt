package com.paulcraciunas.domain.impl.achievements

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.AchievementState
import com.paulcraciunas.domain.api.achievements.GetAchievementState
import com.paulcraciunas.user.api.UserRepository
import javax.inject.Inject

class GetAchievementStateImpl @Inject constructor(
    private val userRepository: UserRepository,
) : GetAchievementState {

    override suspend fun invoke(): List<AchievementState> {
        val user = userRepository.get()
        val progress = user.achievements.progress
        val unseen = user.achievements.unseenAchievements

        return Achievement.entries.map { achievement ->
            val progressValue = progress[achievement.name] ?: 0L
            val tier = achievement.tierFrom(progressValue)

            AchievementState(
                achievement = achievement,
                currentTier = tier,
                currentProgress = progressValue,
                nextThreshold = achievement.nextTierProgress(tier),
                isUnseen = unseen.contains(achievement.name),
            )
        }
    }
}
