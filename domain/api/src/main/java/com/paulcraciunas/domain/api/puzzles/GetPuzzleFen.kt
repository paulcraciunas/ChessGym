package com.paulcraciunas.domain.api.puzzles

interface GetPuzzleFen {
    suspend operator fun invoke(puzzleId: Int): String?
}
