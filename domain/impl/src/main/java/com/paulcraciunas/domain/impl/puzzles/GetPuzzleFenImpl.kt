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
        // It's important to make the opponent move before we get the position for the player's move
        puzzle.start()
        puzzle.playNextMove()
        // Now we can serialize the position
        return fenSerializer.of(puzzle)
    }
}
