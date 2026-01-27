package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.GetPuzzleByRating
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.game.logic.api.Puzzle
import java.util.ArrayDeque
import java.util.Deque
import javax.inject.Inject

/**
 * Implementation of [GetBufferedPuzzleSeries] that loads puzzles in batches.
 *
 * Puzzles are loaded with increasing ratings, similar to [GetPuzzleSeriesImpl],
 * but in smaller batches for memory efficiency.
 */
class GetBufferedPuzzleSeriesImpl @Inject constructor(
    private val getPuzzleByRating: GetPuzzleByRating,
    private val randomFactory: RandomFactory,
) : GetBufferedPuzzleSeries {

    private val buffer: Deque<Puzzle> = ArrayDeque()

    private var batchSize = GetBufferedPuzzleSeries.BATCH_SIZE
    private var increment = GetBufferedPuzzleSeries.INCREMENT
    private var currentRating = GetBufferedPuzzleSeries.RATING_START
    private var exhausted = false

    override operator fun invoke(batchSize: Int, ratingStart: Int, increment: Int) {
        buffer.clear()
        this.batchSize = batchSize
        this.currentRating = ratingStart
        this.increment = increment
        this.exhausted = false
    }

    override suspend fun next(): Puzzle? {
        if (buffer.isEmpty() && !exhausted) {
            loadNextBatch()
        }
        return buffer.pollFirst()
    }

    private suspend fun loadNextBatch() {
        repeat(batchSize) {
            try {
                val puzzle = getPuzzleByRating(currentRating)
                buffer.addLast(puzzle)
                currentRating += randomFactory.nextInt(1, increment)
            } catch (_: Exception) {
                exhausted = true
                return
            }
        }
    }
}
