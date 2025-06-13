package com.paulcraciunas.puzzles.impl.impl

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.PuzzleRepository
import javax.inject.Inject

class PuzzleRepositoryImpl @Inject constructor(
    private val db: PuzzleDatabase,
    private val adapter: PuzzleAdapter,
) : PuzzleRepository {

    override suspend fun getByRating(targetRating: Int): Puzzle? = db.getByRating(targetRating)?.let { adapter.adapt(it) }

    override suspend fun get(count: Int): List<Puzzle> = db.get(count).map { adapter.adapt(it) }

    override suspend fun getByRatingRange(min: Int, max: Int): Puzzle? = db.getInRatingRange(min, max)?.let { adapter.adapt(it) }
}
