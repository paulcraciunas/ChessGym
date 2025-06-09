package com.paulcraciunas.puzzles.impl.usecases

import com.paulcraciunas.game.logic.api.IPuzzle
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.puzzles.api.usecases.GetPuzzleSeries
import com.paulcraciunas.puzzles.impl.impl.RandomFactory
import javax.inject.Inject

internal class GetPuzzleSeriesImpl @Inject constructor(
    private val repository: PuzzleRepository,
    private val randomFactory: RandomFactory
) : GetPuzzleSeries {

    override suspend fun invoke(count: Int, increment: Int, from: Int): List<IPuzzle> {
        val result = mutableListOf<IPuzzle>()
        var rating = from

        repeat(count) {
            repository.getByRating(rating)?.let {
                result.add(it)
                rating += randomFactory.nextInt(1, GetPuzzleSeries.INCREMENT)
            } ?: return@repeat // If we can't find a desired puzzle, we have to stop
        }
        return result
    }
}
