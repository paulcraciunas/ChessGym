package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.OnPuzzleComplete
import com.paulcraciunas.domain.api.PuzzleCompletionResult
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementation of OnPuzzleComplete that updates user data.
 *
 * Integrates with UserRepository to update ratings, statistics, and history.
 */
class OnPuzzleCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
) : OnPuzzleComplete {

    override suspend operator fun invoke(completionResult: PuzzleCompletionResult) {
        val currentUser = userRepository.get()
        val won = completionResult.wasSuccessful
        val ratingChange = completionResult.ratingChange * if (won) 1 else -1

        // Update puzzles played and solved count
        val newPuzzlesPlayed = currentUser.statistics.puzzlesPlayed + 1
        val newPuzzlesSolved = currentUser.statistics.puzzlesSolved + if (won) 1 else 0

        // Update current rating
        val newCurrentRating = currentUser.ratings.current + ratingChange

        // Update best rating if this is a new personal best
        val newBestRating = maxOf(currentUser.highScores.ratedPuzzle, newCurrentRating)

        // Update total time spent (convert to milliseconds)
        val newTotalTimeSpent = currentUser.statistics.totalTimeSpent + completionResult.timeSpentMillis

        // Update failed puzzles list if the puzzle was failed and has an ID
        val newFailedPuzzles = if (!won && completionResult.puzzleId != null) {
            currentUser.failedPuzzles + completionResult.puzzleId!!
        } else {
            currentUser.failedPuzzles
        }

        // Create updated user
        val updatedUser = currentUser.copy(
            ratings = currentUser.ratings.copy(current = newCurrentRating),
            highScores = currentUser.highScores.copy(ratedPuzzle = newBestRating),
            statistics = currentUser.statistics.copy(
                puzzlesPlayed = newPuzzlesPlayed,
                puzzlesSolved = newPuzzlesSolved,
                totalTimeSpent = newTotalTimeSpent
            ),
            failedPuzzles = newFailedPuzzles
        )

        // Create history entry for today
        val today = LocalDate.now()
        val historyData = User.HistoryItem.HistoryItemData.RatedPuzzleData(
            puzzlesPlayed = 1,
            puzzlesSolved = if (won) 1 else 0,
            ratingChange = ratingChange,
            timeSpent = completionResult.timeSpentMillis
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
