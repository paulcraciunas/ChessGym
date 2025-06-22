package com.paulcraciunas.puzzles.api.usecases

import com.paulcraciunas.game.logic.api.Puzzle

interface GetPuzzleByRating {
    suspend operator fun invoke(targetRating: Int): Puzzle
}
