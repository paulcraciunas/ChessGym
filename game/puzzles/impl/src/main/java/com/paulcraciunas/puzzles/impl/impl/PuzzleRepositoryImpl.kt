package com.paulcraciunas.puzzles.impl.impl

import com.paulcraciunas.game.logic.api.IPuzzle
import com.paulcraciunas.puzzles.api.PuzzleRepository
import javax.inject.Inject

class PuzzleRepositoryImpl @Inject constructor(
    private val db: PuzzleDatabase,
    private val adapter: PuzzleAdapter,
) : PuzzleRepository {

    override suspend fun getByRating(targetRating: Int): IPuzzle? = db.getByRating(targetRating)?.let { adapter.adapt(it) }

    override suspend fun get(count: Int): List<IPuzzle> = db.get(count).map { adapter.adapt(it) }

    override suspend fun getByRatingRange(min: Int, max: Int): IPuzzle? = db.getInRatingRange(min, max)?.let { adapter.adapt(it) }
}
