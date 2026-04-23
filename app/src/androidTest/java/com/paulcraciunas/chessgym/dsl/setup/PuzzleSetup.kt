package com.paulcraciunas.chessgym.dsl.setup

import com.paulcraciunas.chessgym.di.TestPuzzleInterceptor
import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.game.logic.api.Puzzle

/**
 * Controls how puzzles are selected during instrumentation tests.
 *
 * The production `GetRatedPuzzle` uses a [com.paulcraciunas.domain.api.general.RandomFactory] to
 * pick a puzzle within a rating window around the user's rating. In tests, this factory is
 * swapped for a [FixedRandomFactory] so we can deterministically target any puzzle present in
 * `test_puzzles.db` by setting the rating used to query it.
 *
 * The currently loaded puzzle (mutable, as played by the ViewModel) is exposed via
 * [currentPuzzle] so that test actions can query the next expected move or the side to move.
 */
class PuzzleSetup(private val randomFactory: FixedRandomFactory) {

    /**
     * Forces [com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle] to pick the puzzle with
     * exactly the given [rating] from `test_puzzles.db`. The DB contains one puzzle per rating
     * in `[1000, 1500]`.
     */
    fun withRating(rating: Int): PuzzleSetup = apply {
        randomFactory.returnValue = rating
    }

    /**
     * The puzzle currently held by the `RatedPuzzleViewModel`. Tests can use this to peek at
     * [Puzzle.nextExpectedMove] and orchestrate correct/incorrect moves.
     *
     * Returns `null` until the rated puzzle screen has loaded its first puzzle.
     */
    val currentPuzzle: Puzzle?
        get() = TestPuzzleInterceptor.currentPuzzle

    /**
     * Clears state between tests. The random factory is reset to `0` (which no puzzle in the
     * test DB matches) to surface any test that forgets to configure a rating, and the
     * captured puzzle is cleared so stale data from a previous test cannot leak in.
     */
    internal fun reset() {
        randomFactory.returnValue = 0
        TestPuzzleInterceptor.currentPuzzle = null
    }
}
