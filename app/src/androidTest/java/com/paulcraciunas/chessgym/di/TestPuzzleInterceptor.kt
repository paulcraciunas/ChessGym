package com.paulcraciunas.chessgym.di

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.PuzzleInterceptor
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestPuzzleInterceptor @Inject constructor() : PuzzleInterceptor {
    override fun intercept(puzzles: List<Puzzle>) {
        Timber.e("intercepting puzzles of size: ${puzzles.size}")
        currentPuzzle = puzzles.lastOrNull()
        loadCount++
    }

    override fun intercept(puzzle: Puzzle) {
        Timber.e("intercepting puzzle id = ${puzzle.id} and rating = ${puzzle.rating}")
        currentPuzzle = puzzle
        loadCount++
    }

    companion object {
        @Volatile
        var currentPuzzle: Puzzle? = null

        @Volatile
        var loadCount: Int = 0
    }
}
