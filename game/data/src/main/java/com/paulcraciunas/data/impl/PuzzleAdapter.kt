package com.paulcraciunas.data.impl

import com.paulcraciunas.data.db.Puzzle
import com.paulcraciunas.game.io.api.PuzzleReader
import javax.inject.Inject

// TODO Paul: test me
class PuzzleAdapter @Inject constructor(
    private val puzzleReader: PuzzleReader
) {
    fun adapt(puzzle: Puzzle): DomainPuzzle {
        val (fen, moves) = puzzleReader.read(puzzle.fenBinary).split(",")
        return DomainPuzzle(fen, moves, puzzle.rating)
    }
}
