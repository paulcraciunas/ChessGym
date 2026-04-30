package com.paulcraciunas.domain.impl.achievements

import com.paulcraciunas.domain.api.achievements.MarkAchievementsSeen
import com.paulcraciunas.user.api.UserRepository
import javax.inject.Inject

class MarkAchievementsSeenImpl @Inject constructor(
    private val userRepository: UserRepository,
) : MarkAchievementsSeen {

    override suspend fun invoke() {
        val user = userRepository.get()
        if (user.achievements.unseenAchievements.isNotEmpty()) {
            val updated = user.copy(
                achievements = user.achievements.copy(unseenAchievements = emptySet())
            )
            userRepository.update(updated)
        }
    }
}
