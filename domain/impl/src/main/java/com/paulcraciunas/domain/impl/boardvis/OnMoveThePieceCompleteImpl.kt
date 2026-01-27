package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.boardvis.MoveThePieceResult
import com.paulcraciunas.domain.api.boardvis.OnMoveThePieceComplete
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementation of [OnMoveThePieceComplete] that updates user data after a game session.
 *
 * - In training mode: Only updates time spent
 * - In non-training mode: Updates both high score (if applicable) and time spent
 */
class OnMoveThePieceCompleteImpl @Inject constructor(
    private val userRepository: UserRepository
) : OnMoveThePieceComplete {

    override suspend fun invoke(result: MoveThePieceResult) {
        val currentUser = userRepository.get()

        // Update high score only if not in training mode and score is higher
        val newHighScore = if (result.isTrainingMode) {
            currentUser.highScores.moveThePiece
        } else {
            maxOf(currentUser.highScores.moveThePiece, result.score)
        }

        // Create updated user with potentially new high score
        val updatedUser = currentUser.copy(
            highScores = currentUser.highScores.copy(moveThePiece = newHighScore)
        )

        // Create history entry for today
        val today = LocalDate.now()
        val historyData = User.HistoryItem.HistoryItemData.BoardVisualizationData(
            sessionsCompleted = 1,
            timeSpent = result.timeSpentMillis
        )
        val historyItem = User.HistoryItem(
            timestamp = today,
            data = historyData
        )

        // Update user and log history
        userRepository.update(updatedUser)
        userRepository.logHistory(listOf(historyItem))
    }
}
