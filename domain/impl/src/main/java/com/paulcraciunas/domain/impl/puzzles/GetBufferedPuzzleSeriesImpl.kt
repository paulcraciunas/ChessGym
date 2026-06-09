package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.GetPuzzleByRating
import com.paulcraciunas.domain.api.puzzles.PuzzleGenerationException
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.global.qualifiers.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GetBufferedPuzzleSeriesImpl @Inject constructor(
    private val getPuzzleByRating: GetPuzzleByRating,
    private val randomFactory: RandomFactory,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GetBufferedPuzzleSeries {

    override fun execute(bufferSize: Int, ratingStart: Int, increment: Int): Flow<Puzzle> = flow {
        var currentRating = ratingStart
        var consecutiveFailures = 0

        while (true) {
            try {
                val puzzle = getPuzzleByRating(currentRating)
                emit(puzzle)
                currentRating += randomFactory.nextInt(1, increment + 1)
                consecutiveFailures = 0
            } catch (e: CancellationException) {
                throw e // Mandatory for coroutine cooperative cancellation
            } catch (e: Exception) {
                consecutiveFailures++
                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    throw PuzzleGenerationException("Failed to load puzzles after multiple attempts", e)
                }
                delay(RETRY_BACKOFF_MS) // Back-off slightly to avoid spinning CPU on instant database failures
            }
        }
    }.flowOn(ioDispatcher)
        .buffer(capacity = bufferSize)

    companion object {
        private const val MAX_CONSECUTIVE_FAILURES = 3
        private const val RETRY_BACKOFF_MS = 200L
    }
}
