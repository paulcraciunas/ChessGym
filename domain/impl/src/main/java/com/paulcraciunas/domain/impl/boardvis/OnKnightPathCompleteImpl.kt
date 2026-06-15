package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.boardvis.KnightPathResult
import com.paulcraciunas.domain.api.boardvis.OnKnightPathComplete
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class OnKnightPathCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val updateAchievementProgress: UpdateAchievementProgress,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : OnKnightPathComplete {

    override suspend fun invoke(result: KnightPathResult) = withContext(dispatcher) {
        val currentUser = userRepository.get()

        val newHighScore = maxOf(currentUser.highScores.knightPath, result.score)
        val newTimeSpent = currentUser.statistics.totalTimeSpent + result.timeSpentMillis

        val updatedUser = currentUser.copy(
            statistics = currentUser.statistics.copy(
                totalTimeSpent = newTimeSpent,
                knightPathSessions = currentUser.statistics.knightPathSessions + 1,
            ),
            highScores = currentUser.highScores.copy(knightPath = newHighScore),
        )

        val withAchievements = updateAchievementProgress(updatedUser)

        val historyItem = User.HistoryItem(
            timestamp = LocalDate.now(),
            data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                sessionsCompleted = 1,
                timeSpent = result.timeSpentMillis
            )
        )

        userRepository.update(withAchievements)
        userRepository.logHistory(listOf(historyItem))
    }
}
