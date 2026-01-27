package com.paulcraciunas.domain.api

import com.paulcraciunas.domain.api.general.EloResult
import com.paulcraciunas.game.logic.api.Puzzle

interface GetRatedPuzzle {
    suspend operator fun invoke(): Data

    class Data(
        val puzzle: Puzzle,
        val ratingChange: EloResult,
    )
}
