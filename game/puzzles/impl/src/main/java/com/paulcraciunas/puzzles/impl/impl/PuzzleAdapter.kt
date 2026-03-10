package com.paulcraciunas.puzzles.impl.impl

import com.paulcraciunas.game.logic.api.diagnostics.LastLoadedPuzzleLog
import com.paulcraciunas.serializer.api.PuzzleReader
import javax.inject.Inject

typealias DbPuzzle = com.paulcraciunas.puzzles.impl.db.Puzzle
typealias DomainPuzzle = com.paulcraciunas.game.logic.api.Puzzle

class PuzzleAdapter @Inject constructor(
    private val puzzleReader: PuzzleReader
) {
    fun adapt(puzzle: DbPuzzle): DomainPuzzle {
        val domainPuzzle = puzzleReader.readPuzzle(puzzle.rating, puzzle.fenBinary, puzzle.id)
        LastLoadedPuzzleLog.recordFen(puzzle.fenBinary.toString())
        return domainPuzzle
    }
}
