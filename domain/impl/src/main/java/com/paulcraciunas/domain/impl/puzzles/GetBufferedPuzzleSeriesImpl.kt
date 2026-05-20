package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.GetPuzzleByRating
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.global.qualifiers.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Implementation of [GetBufferedPuzzleSeries] that loads puzzles in batches.
 *
 * Puzzles are loaded with increasing ratings, similar to [GetPuzzleSeriesImpl],
 * but in smaller batches for memory efficiency.
 *
 * Note: Both [start] and [next] must be called from the same logical thread (Main).
 */
class GetBufferedPuzzleSeriesImpl @Inject constructor(
    private val getPuzzleByRating: GetPuzzleByRating,
    private val randomFactory: RandomFactory,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GetBufferedPuzzleSeries {
    private var puzzleChannel = Channel<Puzzle>(capacity = 10).apply { close() }
    private var producerJob: Job? = null

    override fun start(scope: CoroutineScope, batchSize: Int, ratingStart: Int, increment: Int) {
        producerJob?.cancel()
        puzzleChannel.close()
        puzzleChannel = Channel(capacity = batchSize)

        producerJob = scope.launch(ioDispatcher) {
            var currentRating = ratingStart
            var consecutiveFailures = 0
            while (isActive) {
                try {
                    val puzzle = getPuzzleByRating(currentRating)
                    puzzleChannel.send(puzzle)
                    currentRating += randomFactory.nextInt(1, increment + 1)
                    consecutiveFailures = 0
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    consecutiveFailures++
                    if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                        puzzleChannel.close(e)
                        return@launch
                    }
                }
            }
        }
    }

    override suspend fun next(): Puzzle = puzzleChannel.receive()

    companion object {
        private const val MAX_CONSECUTIVE_FAILURES = 3
    }
}
