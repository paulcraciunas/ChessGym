package com.paulcraciunas.domain.api

import com.paulcraciunas.game.logic.api.Puzzle

interface GetPuzzleByRating {
    suspend operator fun invoke(targetRating: Int): Puzzle
}
