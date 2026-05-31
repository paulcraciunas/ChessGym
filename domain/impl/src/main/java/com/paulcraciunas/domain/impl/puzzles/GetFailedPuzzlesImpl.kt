package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.global.qualifiers.IoDispatcher
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Implementation of [GetFailedPuzzles] that loads failed puzzles in a buffer.
 *
 * Puzzles are loaded by their IDs from the user's failed puzzles list,
 * in smaller batches for memory efficiency.
 */
class GetFailedPuzzlesImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val puzzleRepository: PuzzleRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GetFailedPuzzles {
    private var puzzleChannel = Channel<Puzzle>(capacity = 10).apply { close() }
    private var producerJob: Job? = null
    private var totalPuzzleCount = 0

    override suspend fun load(scope: CoroutineScope, bufferSize: Int) {
        producerJob?.cancel()
        puzzleChannel.close()
        puzzleChannel = Channel(capacity = bufferSize)

        producerJob = scope.launch(ioDispatcher) {
            val failedPuzzles = userRepository.get().failedPuzzles
            totalPuzzleCount = failedPuzzles.size
            failedPuzzles.forEach { id ->
                ensureActive()
                puzzleRepository.getById(id)?.let { puzzleChannel.send(it) }
            }
        }
    }

    override suspend fun next(): Puzzle? = if (producerJob?.isActive == true) {
        puzzleChannel.receiveCatching().getOrNull()
    } else {
        puzzleChannel.tryReceive().getOrNull()
    }
    override fun totalCount(): Int = totalPuzzleCount
}
