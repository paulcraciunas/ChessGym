package com.paulcraciunas.puzzles.impl.impl

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.diagnostics.LastLoadedPuzzleLog
import com.paulcraciunas.puzzles.api.PuzzleInterceptor
import timber.log.Timber
import javax.inject.Inject

class LoggingPuzzleInterceptor @Inject constructor() : PuzzleInterceptor {
    override fun intercept(puzzles: List<Puzzle>) {
        Timber.d("Loaded %d puzzles", puzzles.size)
        puzzles.lastOrNull()?.let { recordPuzzle(it) }
    }

    override fun intercept(puzzle: Puzzle) {
        Timber.d("Loaded puzzle id=%d, rating=%d", puzzle.id, puzzle.rating)
        recordPuzzle(puzzle)
    }

    private fun recordPuzzle(puzzle: Puzzle) {
        LastLoadedPuzzleLog.record(id = puzzle.id, rating = puzzle.rating)
    }
}
