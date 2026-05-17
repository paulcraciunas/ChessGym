package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.puzzles.GetPuzzleByRating
import com.paulcraciunas.domain.api.puzzles.GetPuzzleSeries
import com.paulcraciunas.game.logic.api.Puzzle
import javax.inject.Inject

class GetPuzzleSeriesImpl @Inject constructor(
    private val getPuzzleByRating: GetPuzzleByRating,
    private val randomFactory: RandomFactory,
) : GetPuzzleSeries {

    override suspend fun invoke(count: Int, increment: Int, from: Int): List<Puzzle> {
        val result = mutableListOf<Puzzle>()
        var rating = from

        repeat(count) {
            try {
                // getPuzzleByRating uses rating expanding logic. We need to make sure we didn't previously add this puzzle
                val puzzle = getPuzzleByRating(rating)
                if (puzzle.rating == result.lastOrNull()?.rating)
                    return@repeat // Can't have 2 puzzles with the same rating in a series
                result.add(puzzle)
                rating += randomFactory.nextInt(1, increment)
            } catch (_: IllegalArgumentException) {
                return result
            }
        }
        return result
    }
}
