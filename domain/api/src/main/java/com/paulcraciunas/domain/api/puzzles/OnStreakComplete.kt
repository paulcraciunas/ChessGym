package com.paulcraciunas.domain.api.puzzles

/**
 * Use case for handling when a puzzle streak ends (puzzle failed).
 *
 * This use case is responsible for:
 * - Updating high score if the current streak is a new personal best
 * - Resetting the current streak count to 0
 * - Clearing the last puzzle ID
 * - Logging a User.HistoryItem
 */
interface OnStreakComplete {
    /**
     * Processes the end of a puzzle streak.
     *
     * @param timeSpentMillis total time spent in the streak session
     * @return the result of the streak completion
     */
    suspend operator fun invoke(timeSpentMillis: Long): StreakCompleteResult

    data class StreakCompleteResult(
        val isNewHighScore: Boolean,
        val finalStreakCount: Int,
    )
}
