package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

class OnStreakCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val updateAchievementProgress: UpdateAchievementProgress,
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
                puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
            ),
            statistics = currentUser.statistics.copy(
                streakSessions = currentUser.statistics.streakSessions + 1,
            ),
        )

        val withAchievements = updateAchievementProgress(updatedUser)

        val historyItem = User.HistoryItem(
            timestamp = LocalDate.now(),
            data = User.HistoryItem.HistoryItemData.PuzzleStreakData(
                finalStreakCount = finalStreakCount,
                timeSpent = timeSpentMillis,
            )
        )

        userRepository.update(withAchievements)
        userRepository.logHistory(listOf(historyItem))

        return OnStreakComplete.StreakCompleteResult(
            isNewHighScore = isNewHighScore,
            finalStreakCount = finalStreakCount
        )
    }
}
