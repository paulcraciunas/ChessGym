package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.domain.api.puzzles.PuzzleAnalysisData
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.serializer.api.Serializer

class GetPuzzleFenImpl(
    private val puzzleRepository: PuzzleRepository,
    private val fenSerializer: Serializer,
) : GetPuzzleFen {

    override suspend fun invoke(puzzleId: Int): PuzzleAnalysisData? {
        val puzzle = puzzleRepository.getById(puzzleId) ?: return null
        puzzle.start()
        val firstMove = puzzle.expectedMoves.firstOrNull() ?: return null
        val fen = fenSerializer.of(puzzle)
        return PuzzleAnalysisData(fen = fen, firstMove = firstMove)
    }
}
