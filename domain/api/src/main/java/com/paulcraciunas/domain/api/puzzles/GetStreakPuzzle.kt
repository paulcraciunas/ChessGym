package com.paulcraciunas.domain.api.puzzles

import com.paulcraciunas.game.logic.api.Puzzle

/**
 * Use case for fetching the next puzzle in streak mode.
 *
 * If the user has an active streak, this will continue from where they left off.
 * Otherwise, it starts a new streak from the base rating.
 */
interface GetStreakPuzzle {
    /**
     * Fetches the next puzzle for the streak.
     *
     * @return Data containing the puzzle and current streak count
     * @throws Exception if no puzzle is available
     */
    suspend operator fun invoke(): Data

    data class Data(
        val puzzle: Puzzle,
        val currentStreakCount: Int,
    )

    companion object {
        const val BASE_RATING = 400
        const val RATING_INCREMENT = 50
    }
}
