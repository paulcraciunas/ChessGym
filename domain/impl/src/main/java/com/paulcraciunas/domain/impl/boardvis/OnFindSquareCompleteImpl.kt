package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.boardvis.FindSquareResult
import com.paulcraciunas.domain.api.boardvis.OnFindSquareComplete
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class OnFindSquareCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val updateAchievementProgress: UpdateAchievementProgress,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : OnFindSquareComplete {

    override suspend fun invoke(result: FindSquareResult) = withContext(dispatcher) {
        val currentUser = userRepository.get()

        val newHighScore = maxOf(currentUser.highScores.findTheSquare, result.score)
        val newTimeSpent = currentUser.statistics.totalTimeSpent + result.timeSpentMillis

        val updatedUser = currentUser.copy(
            statistics = currentUser.statistics.copy(
                totalTimeSpent = newTimeSpent,
                findSquareSessions = currentUser.statistics.findSquareSessions + 1,
            ),
            highScores = currentUser.highScores.copy(findTheSquare = newHighScore),
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
