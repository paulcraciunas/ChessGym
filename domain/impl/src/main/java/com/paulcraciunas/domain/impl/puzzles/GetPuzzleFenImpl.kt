package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.serializer.api.Serializer

class GetPuzzleFenImpl(
    private val puzzleRepository: PuzzleRepository,
    private val fenSerializer: Serializer,
) : GetPuzzleFen {

    override suspend fun invoke(puzzleId: Int): String? {
        val puzzle = puzzleRepository.getById(puzzleId) ?: return null
        return fenSerializer.of(puzzle)
    }
}
