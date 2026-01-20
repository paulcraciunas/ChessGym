package com.paulcraciunas.domain.api

import com.paulcraciunas.game.logic.api.Puzzle

/**
 * Provides puzzles one at a time, loading them in batches for efficiency.
 *
 * Usage:
 * ```
 * puzzleSeries(batchSize = 10, ratingStart = 400, increment = 60)
 * while (true) {
 *     val puzzle = puzzleSeries.next() ?: break
 *     // Use puzzle
 * }
 * ```
 *
 * Note: This class is not Thread-safe!
 */
interface GetBufferedPuzzleSeries {
    /**
     * Resets and configures the puzzle series.
     *
     * Clears any buffered puzzles and configures new parameters.
     *
     * @param batchSize Number of puzzles to load per batch
     * @param ratingStart Starting rating for puzzles
     * @param increment Maximum rating increment between puzzles
     */
    operator fun invoke(
        batchSize: Int = BATCH_SIZE,
        ratingStart: Int = RATING_START,
        increment: Int = INCREMENT,
    )

    /**
     * Returns the next puzzle, loading a new batch if needed.
     *
     * @return The next puzzle, or null if no more puzzles are available
     */
    suspend fun next(): Puzzle?

    companion object Defaults {
        const val BATCH_SIZE = 10
        const val RATING_START = 400
        const val INCREMENT = 60
    }
}
