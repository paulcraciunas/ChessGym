package com.paulcraciunas.puzzles.api

import com.paulcraciunas.game.logic.api.Puzzle

/**
 * Interceptor for loaded puzzles. Each time a puzzle or a list of puzzles is/are loaded
 * from the repository, the interceptor is called.
 *
 * Multiple interceptors can be registered via Hilt, and they will be called in order.
 */
interface PuzzleInterceptor {
    fun intercept(puzzles: List<Puzzle>)
    fun intercept(puzzle: Puzzle)
}
