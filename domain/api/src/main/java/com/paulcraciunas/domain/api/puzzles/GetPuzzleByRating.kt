package com.paulcraciunas.domain.api.puzzles

import com.paulcraciunas.game.logic.api.Puzzle

interface GetPuzzleByRating {
    suspend operator fun invoke(targetRating: Int): Puzzle
}
