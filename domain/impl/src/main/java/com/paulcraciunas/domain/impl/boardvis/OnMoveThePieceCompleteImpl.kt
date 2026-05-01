package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.boardvis.MoveThePieceResult
import com.paulcraciunas.domain.api.boardvis.OnMoveThePieceComplete
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

class OnMoveThePieceCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val updateAchievementProgress: UpdateAchievementProgress,
) : OnMoveThePieceComplete {

    override suspend fun invoke(result: MoveThePieceResult) {
        val currentUser = userRepository.get()

        val newHighScore = if (result.isTrainingMode) {
            currentUser.highScores.moveThePiece
        } else {
            maxOf(currentUser.highScores.moveThePiece, result.score)
        }
        val newTimeSpent = currentUser.statistics.totalTimeSpent + result.timeSpentMillis

        val updatedUser = currentUser.copy(
            statistics = currentUser.statistics.copy(
                totalTimeSpent = newTimeSpent,
                moveThePieceSessions = currentUser.statistics.moveThePieceSessions + 1,
            ),
            highScores = currentUser.highScores.copy(moveThePiece = newHighScore),
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
