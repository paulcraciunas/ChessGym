package com.paulcraciunas.domain.api.puzzles

data class PuzzleAnalysisData(
    val fen: String,
    val firstMove: String,
)

interface GetPuzzleFen {
    suspend operator fun invoke(puzzleId: Int): PuzzleAnalysisData?
}
