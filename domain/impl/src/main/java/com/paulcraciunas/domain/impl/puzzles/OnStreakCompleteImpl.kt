package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementation of [OnStreakComplete] that handles the end of a puzzle streak.
 *
 * Updates:
 * - High score if the final streak count is a new personal best
 * - Resets current streak count to 0
 * - Clears the last puzzle ID
 * - Logs a [User.HistoryItem.HistoryItemData.PuzzleStreakData] history item
 */
class OnStreakCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
) : OnStreakComplete {

    override suspend fun invoke(timeSpentMillis: Long): OnStreakComplete.StreakCompleteResult {
        val currentUser = userRepository.get()
        val finalStreakCount = currentUser.ratings.puzzleStreak.currentCount
        val previousHighScore = currentUser.highScores.puzzleStreak
        val isNewHighScore = finalStreakCount > previousHighScore

        val newHighScore = if (isNewHighScore) finalStreakCount else previousHighScore

        val updatedUser = currentUser.copy(
            highScores = currentUser.highScores.copy(puzzleStreak = newHighScore),
            ratings = currentUser.ratings.copy(
                puzzleStreak = User.PuzzleStreak(
                    currentCount = 0,
                    lastPuzzleId = null,
                )
            )
        )

        val historyItem = User.HistoryItem(
            timestamp = LocalDate.now(),
            data = User.HistoryItem.HistoryItemData.PuzzleStreakData(
                finalStreakCount = finalStreakCount,
                timeSpent = timeSpentMillis,
            )
        )

        userRepository.update(updatedUser)
        userRepository.logHistory(listOf(historyItem))

        return OnStreakComplete.StreakCompleteResult(
            isNewHighScore = isNewHighScore,
            finalStreakCount = finalStreakCount
        )
    }
}
