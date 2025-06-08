package com.paulcraciunas.data.impl

import com.paulcraciunas.data.db.Puzzle
import com.paulcraciunas.serializer.api.PuzzleReader
import javax.inject.Inject

//TODO Paul: This is only temporary. Delete this and reimplement it properly
class PuzzleAdapter @Inject constructor(
    private val puzzleReader: PuzzleReader
) {
    fun adapt(puzzle: Puzzle): DomainPuzzle {
        return DomainPuzzle(puzzle.fenBinary, puzzle.rating)
    }
}
