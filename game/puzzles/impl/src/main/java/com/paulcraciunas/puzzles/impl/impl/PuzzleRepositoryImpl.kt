package com.paulcraciunas.puzzles.impl.impl

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.PuzzleInterceptor
import com.paulcraciunas.puzzles.api.PuzzleRepository
import timber.log.Timber
import javax.inject.Inject

class PuzzleRepositoryImpl @Inject constructor(
    private val db: PuzzleDatabase,
    private val adapter: PuzzleAdapter,
    private val interceptors: Set<@JvmSuppressWildcards PuzzleInterceptor>,
) : PuzzleRepository {

    override suspend fun get(count: Int): List<Puzzle> = try {
        db.get(count).map { adapter.adapt(it) }.also { puzzles ->
            interceptors.forEach {
                it.intercept(puzzles)
            }
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load %d puzzles from database", count)
        throw e
    }

    override suspend fun getById(id: Int): Puzzle? = try {
        db.getById(id)?.let { adapter.adapt(it) }?.also { puzzle ->
            interceptors.forEach {
                it.intercept(puzzle)
            }
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load puzzle by id=%d", id)
        throw e
    }

    override suspend fun getByRating(targetRating: Int): Puzzle? = try {
        db.getByRating(targetRating)?.let { adapter.adapt(it) }?.also { puzzle ->
            interceptors.forEach {
                it.intercept(puzzle)
            }
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load puzzle by rating=%d", targetRating)
        throw e
    }

    override suspend fun getByRatingRange(min: Int, max: Int): Puzzle? = try {
        db.getInRatingRange(min, max)?.let { adapter.adapt(it) }?.also { puzzle ->
            interceptors.forEach {
                it.intercept(puzzle)
            }
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load puzzle in rating range [%d, %d]", min, max)
        throw e
    }
}
