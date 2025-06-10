package com.paulcraciunas.puzzles.impl.impl

import com.paulcraciunas.game.logic.api.IPuzzle
import com.paulcraciunas.puzzles.impl.db.Puzzle
import com.paulcraciunas.serializer.api.PuzzleReader
import javax.inject.Inject

class PuzzleAdapter @Inject constructor(
    private val puzzleReader: PuzzleReader
) {
    fun adapt(puzzle: Puzzle): IPuzzle {
        return puzzleReader.readPuzzle(puzzle.fenBinary) // TODO Paul: do something with the puzzle.rating
    }
}
