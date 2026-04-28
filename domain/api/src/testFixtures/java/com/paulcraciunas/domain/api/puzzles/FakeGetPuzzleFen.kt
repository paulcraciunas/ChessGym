package com.paulcraciunas.domain.api.puzzles

class FakeGetPuzzleFen : GetPuzzleFen {
    private val fenByPuzzleId = mutableMapOf<Int, String>()

    var lastRequestedId: Int? = null
        private set

    fun setFen(puzzleId: Int, fen: String) {
        fenByPuzzleId[puzzleId] = fen
    }

    override suspend fun invoke(puzzleId: Int): String? {
        lastRequestedId = puzzleId
        return fenByPuzzleId[puzzleId]
    }
}
