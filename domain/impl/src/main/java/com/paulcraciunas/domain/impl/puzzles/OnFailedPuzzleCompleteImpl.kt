package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.OnFailedPuzzleComplete
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementation of [OnFailedPuzzleComplete] that removes the puzzle from the user's failed list.
 *
 * Updates:
 * - Removes the puzzle ID from the failed puzzles list
 * - Increments the puzzles solved count
 * - Logs a [User.HistoryItem.HistoryItemData.FailedPuzzleData] history item
 */
class OnFailedPuzzleCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
) : OnFailedPuzzleComplete {

    override suspend operator fun invoke(puzzleId: Int, timeSpentMillis: Long) {
        val currentUser = userRepository.get()

        val updatedFailedPuzzles = currentUser.failedPuzzles.filter { it != puzzleId }
        val newPuzzlesSolved = currentUser.statistics.puzzlesSolved + 1

        val updatedUser = currentUser.copy(
            failedPuzzles = updatedFailedPuzzles,
            statistics = currentUser.statistics.copy(
                puzzlesSolved = newPuzzlesSolved
            )
        )

        val historyItem = User.HistoryItem(
            timestamp = LocalDate.now(),
            data = User.HistoryItem.HistoryItemData.FailedPuzzleData(
                puzzlesSolved = 1,
                timeSpent = timeSpentMillis,
            )
        )

        userRepository.update(updatedUser)
        userRepository.logHistory(listOf(historyItem))
    }
}
