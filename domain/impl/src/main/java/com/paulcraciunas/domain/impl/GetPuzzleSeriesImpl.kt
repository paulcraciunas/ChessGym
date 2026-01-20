package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.GetPuzzleSeries
import com.paulcraciunas.domain.api.RandomFactory
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.PuzzleRepository
import javax.inject.Inject

class GetPuzzleSeriesImpl @Inject constructor(
    private val repository: PuzzleRepository,
    private val randomFactory: RandomFactory
) : GetPuzzleSeries {

    override suspend fun invoke(count: Int, increment: Int, from: Int): List<Puzzle> {
        val result = mutableListOf<Puzzle>()
        var rating = from

        repeat(count) {
            repository.getByRating(rating)?.let {
                result.add(it)
                rating += randomFactory.nextInt(1, increment)
            } ?: return@repeat // If we can't find a desired puzzle, we have to stop
        }
        return result
    }
}
