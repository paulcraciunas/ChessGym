package com.paulcraciunas.data.impl

import com.paulcraciunas.domain.PuzzleRepository
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject

typealias DomainPuzzle = com.paulcraciunas.domain.Puzzle

// TODO Paul: test me
class PuzzleRepositoryImpl @Inject constructor(
    private val db: PuzzleDatabase,
    private val adapter: PuzzleAdapter,
) : PuzzleRepository {

    override suspend fun getByRating(targetRating: Int): DomainPuzzle? = db.getByRating(targetRating)?.let { adapter.adapt(it) }

    override suspend fun get(count: Int): List<DomainPuzzle> = db.get(count).map { adapter.adapt(it) }

    override suspend fun getByRatingRange(min: Int, max: Int): DomainPuzzle? = db.getInRatingRange(min, max)?.let { adapter.adapt(it) }

    override suspend fun getByIncreasingRatings(
        count: Int,
        increment: Int,
        from: Int
    ): List<DomainPuzzle> {
        val result = mutableListOf<DomainPuzzle>()
        var rating = from

        repeat(count) {
            db.puzzleDao().getByRating(rating)?.let {
                result.add(adapter.adapt(it))
                rating += ThreadLocalRandom.current().nextInt(1, PuzzleRepository.INCREMENT)
            } ?: return@repeat
        }
        return result
    }
}
