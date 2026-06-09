package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.domain.api.puzzles.PuzzleGenerationException
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.global.qualifiers.IoDispatcher
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GetFailedPuzzlesImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val puzzleRepository: PuzzleRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GetFailedPuzzles {
    override fun execute(bufferSize: Int): Flow<Puzzle> = flow {
        var consecutiveFailures = 0
        val puzzleIds = userRepository.get().failedPuzzles
        puzzleIds.forEach { id ->
            try {
                puzzleRepository.getById(id)?.let {
                    emit(it)
                    consecutiveFailures = 0
                }
            } catch (up: CancellationException) {
                throw up // Mandatory for coroutine cooperative cancellation
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
