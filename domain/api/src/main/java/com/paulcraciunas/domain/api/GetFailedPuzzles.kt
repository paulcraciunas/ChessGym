package com.paulcraciunas.domain.api

import com.paulcraciunas.game.logic.api.Puzzle

/**
 * Provides failed puzzles one at a time, loading them in batches for efficiency.
 *
 * Usage:
 * ```
 * failedPuzzles.load()
 * while (true) {
 *     val puzzle = failedPuzzles.next() ?: break
 *     // Use puzzle
 * }
 * ```
 *
 * Note: This class is not Thread-safe!
 */
interface GetFailedPuzzles {
    /**
     * Loads the failed puzzle IDs from the user and clears any previously buffered puzzles.
     *
     * @param batchSize Number of puzzles to load per batch
     */
    suspend fun load(batchSize: Int = BATCH_SIZE)

    /**
     * Returns the next failed puzzle, loading a new batch if needed.
     *
     * @return The next puzzle, or null if no more puzzles are available
     */
    suspend fun next(): Puzzle?

    /**
     * Returns the total count of failed puzzles to retry.
     */
    fun totalCount(): Int

    /**
     * Returns the count of remaining puzzles that haven't been retrieved yet.
     */
    fun remainingCount(): Int

    companion object Defaults {
        const val BATCH_SIZE = 5
    }
}
