package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleRushResult
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementation of OnPuzzleRushComplete that updates user data after a puzzle rush session.
 *
 * Updates:
 * - Puzzle rush high score (if new best)
 * - Total puzzles played and solved statistics
 * - Total time spent
 * - Failed puzzle IDs for retry
 * - Daily history entry
 */
class OnPuzzleRushCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
) : OnPuzzleRushComplete {

    override suspend operator fun invoke(result: PuzzleRushResult) {
        val currentUser = userRepository.get()

        // Update puzzles played and solved count
        val totalPuzzles = result.puzzlesSolved + result.puzzlesFailed
        val newPuzzlesPlayed = currentUser.statistics.puzzlesPlayed + totalPuzzles
        val newPuzzlesSolved = currentUser.statistics.puzzlesSolved + result.puzzlesSolved

        // Update puzzle rush high score if this is a new personal best
        val newPuzzleRushHighScore = maxOf(currentUser.highScores.puzzleRush, result.puzzlesSolved)

        // Update total time spent
        val newTotalTimeSpent = currentUser.statistics.totalTimeSpent + result.timeSpentMillis

        // Add failed puzzle IDs to the retry list (avoiding duplicates)
        val newFailedPuzzles = (currentUser.failedPuzzles + result.failedPuzzleIds).distinct()

        // Create updated user
        val updatedUser = currentUser.copy(
            highScores = currentUser.highScores.copy(puzzleRush = newPuzzleRushHighScore),
            statistics = currentUser.statistics.copy(
                puzzlesPlayed = newPuzzlesPlayed,
                puzzlesSolved = newPuzzlesSolved,
                totalTimeSpent = newTotalTimeSpent
            ),
            failedPuzzles = newFailedPuzzles
        )

        // Create history entry for today
        val today = LocalDate.now()
        val historyData = User.HistoryItem.HistoryItemData.PuzzleRushData(
            tries = 1,
            bestScore = result.puzzlesSolved,
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
