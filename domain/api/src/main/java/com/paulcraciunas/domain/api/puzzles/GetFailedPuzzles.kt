package com.paulcraciunas.domain.api.puzzles

import com.paulcraciunas.game.logic.api.Puzzle
import kotlinx.coroutines.flow.Flow

/**
 * Provides a series of failed puzzles stored in the user object, loading them in a buffer for efficiency.
 * Runs on the I/O Dispatcher.
 */
interface GetFailedPuzzles {
    fun execute(bufferSize: Int = BUFFER_SIZE): Flow<Puzzle>

    companion object Defaults {
        const val BUFFER_SIZE = 3
    }
}
