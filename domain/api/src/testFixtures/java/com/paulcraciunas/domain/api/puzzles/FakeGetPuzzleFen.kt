package com.paulcraciunas.domain.api.puzzles

class FakeGetPuzzleFen : GetPuzzleFen {
    private val dataByPuzzleId = mutableMapOf<Int, PuzzleAnalysisData>()

    var lastRequestedId: Int? = null
        private set

    override suspend fun invoke(puzzleId: Int): PuzzleAnalysisData? {
        lastRequestedId = puzzleId
        return dataByPuzzleId[puzzleId]
    }
}
