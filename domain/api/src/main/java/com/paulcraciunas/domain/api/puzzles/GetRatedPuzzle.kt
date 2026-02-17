package com.paulcraciunas.domain.api.puzzles

import com.paulcraciunas.domain.api.general.EloResult
import com.paulcraciunas.game.logic.api.Puzzle

interface GetRatedPuzzle {
    /**
     * By default, will get a puzzle +/- [DEVIATION] rating points away from the user's rating.
     **/
    suspend operator fun invoke(): Data

    class Data(
        val puzzle: Puzzle,
        val ratingChange: EloResult,
    )

    companion object {
        const val DEVIATION = 25
    }
}
