package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.puzzles.OnPuzzleComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleCompletionResult
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

class OnPuzzleCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val updateAchievementProgress: UpdateAchievementProgress,
) : OnPuzzleComplete {

    override suspend operator fun invoke(completionResult: PuzzleCompletionResult) {
        val currentUser = userRepository.get()
        val won = completionResult.wasSuccessful
        val ratingChange = completionResult.ratingChange * if (won) 1 else -1

        val newPuzzlesPlayed = currentUser.statistics.puzzlesPlayed + 1
        val newPuzzlesSolved = currentUser.statistics.puzzlesSolved + if (won) 1 else 0
        val newCurrentRating = currentUser.ratings.current + ratingChange
        val newBestRating = maxOf(currentUser.highScores.ratedPuzzle, newCurrentRating)
        val newTotalTimeSpent = currentUser.statistics.totalTimeSpent + completionResult.timeSpentMillis

        val newFailedPuzzles = if (!won && completionResult.puzzleId != null) {
            currentUser.failedPuzzles + completionResult.puzzleId!!
        } else {
            currentUser.failedPuzzles
        }

        val achievements = currentUser.achievements
        val winStreak = if (won) achievements.currentRatedWinStreak + 1 else 0
        val bestWinStreak = maxOf(achievements.bestRatedWinStreak, winStreak)

        val updatedUser = currentUser.copy(
            ratings = currentUser.ratings.copy(current = newCurrentRating),
            highScores = currentUser.highScores.copy(ratedPuzzle = newBestRating),
            statistics = currentUser.statistics.copy(
                puzzlesPlayed = newPuzzlesPlayed,
                puzzlesSolved = newPuzzlesSolved,
                totalTimeSpent = newTotalTimeSpent,
                ratedPuzzlesSolved = currentUser.statistics.ratedPuzzlesSolved + if (won) 1 else 0,
            ),
            failedPuzzles = newFailedPuzzles,
            achievements = achievements.copy(
                currentRatedWinStreak = winStreak,
                bestRatedWinStreak = bestWinStreak,
            ),
        )

        val withAchievements = updateAchievementProgress(updatedUser)

        val today = LocalDate.now()
        val historyItem = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                puzzlesPlayed = 1,
                puzzlesSolved = if (won) 1 else 0,
                ratingChange = ratingChange,
                timeSpent = completionResult.timeSpentMillis
            )
        )

        userRepository.update(withAchievements)
        userRepository.logHistory(listOf(historyItem))
    }
}
