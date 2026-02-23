package com.paulcraciunas.domain.api.puzzles

/**
 * Use case for handling a successfully solved failed puzzle.
 *
 * This use case is responsible for:
 * - Removing the puzzle from the user's failed puzzles list
 * - Updating puzzles solved statistics
 * - Logging a [com.paulcraciunas.user.api.User.HistoryItem.HistoryItemData.FailedPuzzleData] history item
 */
interface OnFailedPuzzleComplete {

    /**
     * Removes the completed puzzle from the failed puzzles list and logs history.
     *
     * @param puzzleId The ID of the successfully solved puzzle
     * @param timeSpentMillis time spent solving the puzzle in milliseconds
     */
    suspend operator fun invoke(puzzleId: Int, timeSpentMillis: Long)
}
