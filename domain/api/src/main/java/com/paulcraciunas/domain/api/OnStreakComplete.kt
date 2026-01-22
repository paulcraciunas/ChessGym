package com.paulcraciunas.domain.api

/**
 * Use case for handling when a puzzle streak ends (puzzle failed).
 *
 * This use case is responsible for:
 * - Updating high score if the current streak is a new personal best
 * - Resetting the current streak count to 0
 * - Clearing the last puzzle ID
 *
 * @return true if the streak was a new high score, false otherwise
 */
interface OnStreakComplete {
    /**
     * Processes the end of a puzzle streak.
     *
     * @return true if this was a new high score
     */
    suspend operator fun invoke(): StreakCompleteResult

    data class StreakCompleteResult(
        val isNewHighScore: Boolean,
        val finalStreakCount: Int,
    )
}
