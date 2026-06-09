package com.paulcraciunas.domain.api.puzzles

import com.paulcraciunas.game.logic.api.Puzzle
import kotlinx.coroutines.flow.Flow

/**
 * Provides a series of puzzles, loading them in a buffer for efficiency.
 * Runs on the I/O Dispatcher.
 */
interface GetBufferedPuzzleSeries {
    fun execute(
        bufferSize: Int = BATCH_SIZE,
        ratingStart: Int = RATING_START,
        increment: Int = INCREMENT,
    ): Flow<Puzzle>

    companion object Defaults {
        const val BATCH_SIZE = 10
        const val RATING_START = 400
        const val INCREMENT = 60
    }
}
