package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.GetFailedPuzzles
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.user.api.UserRepository
import java.util.ArrayDeque
import java.util.Deque
import javax.inject.Inject

/**
 * Implementation of [GetFailedPuzzles] that loads failed puzzles in batches.
 *
 * Puzzles are loaded by their IDs from the user's failed puzzles list,
 * in smaller batches for memory efficiency.
 */
class GetFailedPuzzlesImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val puzzleRepository: PuzzleRepository,
) : GetFailedPuzzles {

    private val buffer: Deque<Puzzle> = ArrayDeque()
    private var pendingIds: List<Int> = emptyList()
    private var currentIndex = 0
    private var batchSize = GetFailedPuzzles.BATCH_SIZE
    private var totalPuzzleCount = 0

    override suspend fun load(batchSize: Int) {
        buffer.clear()
        this.batchSize = batchSize
        this.currentIndex = 0

        val user = userRepository.get()
        this.pendingIds = user.failedPuzzles
        this.totalPuzzleCount = pendingIds.size
    }

    override suspend fun next(): Puzzle? {
        if (buffer.isEmpty() && currentIndex < pendingIds.size) {
            loadNextBatch()
        }
        return buffer.pollFirst()
    }

    override fun totalCount(): Int = totalPuzzleCount

    override fun remainingCount(): Int = (pendingIds.size - currentIndex) + buffer.size

    private suspend fun loadNextBatch() {
        val endIndex = minOf(currentIndex + batchSize, pendingIds.size)
        val idsToLoad = pendingIds.subList(currentIndex, endIndex)

        for (id in idsToLoad) {
            val puzzle = puzzleRepository.getById(id)
            if (puzzle != null) {
                buffer.addLast(puzzle)
            }
        }

        currentIndex = endIndex
    }
}
