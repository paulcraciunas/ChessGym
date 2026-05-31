package com.paulcraciunas.domain.api.puzzles

import com.paulcraciunas.game.logic.api.Puzzle
import kotlinx.coroutines.CoroutineScope

/**
 * Provides failed puzzles one at a time, loading them in a buffer for efficiency.
 *
 * Usage:
 * ```
 * failedPuzzles.load()
 * val puzzle = failedPuzzles.next()
 * if (puzzle != null) {
 *     // load next puzzle
 * } else {
 *     // display results
 * }
 * ```
 *
 * Note: This class is not Thread-safe!
 */
interface GetFailedPuzzles {
    /**
     * Loads the failed puzzle IDs from the user and clears any previously buffered puzzles.
     *
     * @param bufferSize Number of puzzles to load per batch
     */
    suspend fun load(scope: CoroutineScope, bufferSize: Int = BUFFER_SIZE)

    /**
     * Returns the next failed puzzle, updating the buffer if needed.
     *
     * @return The next puzzle, or null if no more puzzles are available
     */
    suspend fun next(): Puzzle?
    fun totalCount(): Int

    companion object Defaults {
        const val BUFFER_SIZE = 5
    }
}
