package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.FindSquareResult
import com.paulcraciunas.domain.api.OnFindSquareComplete
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementation of [OnFindSquareComplete] that updates user data after a game session.
 *
 * Updates the user's high score if the current score is higher, and logs
 * the game session to the user's history.
 */
class OnFindSquareCompleteImpl @Inject constructor(
    private val userRepository: UserRepository
) : OnFindSquareComplete {

    override suspend fun invoke(result: FindSquareResult) {
        val currentUser = userRepository.get()

        // Update high score if this is a new personal best
        val newHighScore = maxOf(currentUser.highScores.findTheSquare, result.score)

        // Create updated user with potentially new high score
        val updatedUser = currentUser.copy(
            highScores = currentUser.highScores.copy(findTheSquare = newHighScore)
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
