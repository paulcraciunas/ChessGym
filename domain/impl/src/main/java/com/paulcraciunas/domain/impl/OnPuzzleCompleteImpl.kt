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
    private val userRepository: UserRepository
) : OnPuzzleComplete {

    override suspend operator fun invoke(completionResult: PuzzleCompletionResult) {
        val currentUser = userRepository.get()

        // Update puzzles played and solved count
        val newPuzzlesPlayed = currentUser.statistics.puzzlesPlayed + 1
        val newPuzzlesSolved = if (completionResult.wasSuccessful) {
            currentUser.statistics.puzzlesSolved + 1
        } else {
            currentUser.statistics.puzzlesSolved
        }

        // Update current rating
        val newCurrentRating = currentUser.ratings.current + completionResult.ratingChange

        // Update best rating if this is a new personal best
        val newBestRating = maxOf(currentUser.highScores.ratedPuzzle, newCurrentRating)

        // Update total time spent (convert to milliseconds)
        val newTotalTimeSpent = currentUser.statistics.totalTimeSpent + completionResult.timeSpentSeconds * 1000L

        // Create updated user
        val updatedUser = currentUser.copy(
            ratings = currentUser.ratings.copy(current = newCurrentRating),
            highScores = currentUser.highScores.copy(ratedPuzzle = newBestRating),
            statistics = currentUser.statistics.copy(
                puzzlesPlayed = newPuzzlesPlayed,
                puzzlesSolved = newPuzzlesSolved,
                totalTimeSpent = newTotalTimeSpent
            )
        )

        // Create history entry for today
        val today = LocalDate.now()
        val historyData = User.HistoryItem.HistoryItemData.RatedPuzzleData(
            puzzlesPlayed = 1,
            puzzlesSolved = if (completionResult.wasSuccessful) 1 else 0,
            ratingChange = completionResult.ratingChange,
            timeSpent = completionResult.timeSpentSeconds * 1000L
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
