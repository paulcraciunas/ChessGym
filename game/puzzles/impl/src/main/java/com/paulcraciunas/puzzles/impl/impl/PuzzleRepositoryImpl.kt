package com.paulcraciunas.puzzles.impl.impl

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.diagnostics.LastLoadedPuzzleLog
import com.paulcraciunas.puzzles.api.PuzzleRepository
import timber.log.Timber
import javax.inject.Inject

class PuzzleRepositoryImpl @Inject constructor(
    private val db: PuzzleDatabase,
    private val adapter: PuzzleAdapter,
) : PuzzleRepository {

    override suspend fun get(count: Int): List<Puzzle> = try {
        db.get(count).map { adapter.adapt(it) }.also { puzzles ->
            Timber.d("Loaded %d puzzles", puzzles.size)
            puzzles.lastOrNull()?.let { recordPuzzle(it) }
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load %d puzzles from database", count)
        throw e
    }

    override suspend fun getById(id: Int): Puzzle? = try {
        db.getById(id)?.let { adapter.adapt(it) }?.also { puzzle ->
            Timber.d("Loaded puzzle id=%d, rating=%d", id, puzzle.rating)
            recordPuzzle(puzzle)
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load puzzle by id=%d", id)
        throw e
    }

    override suspend fun getByRating(targetRating: Int): Puzzle? = try {
        db.getByRating(targetRating)?.let { adapter.adapt(it) }?.also { puzzle ->
            Timber.d("Loaded puzzle by rating=%d, actual id=%d", targetRating, puzzle.id)
            recordPuzzle(puzzle)
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load puzzle by rating=%d", targetRating)
        throw e
    }

    override suspend fun getByRatingRange(min: Int, max: Int): Puzzle? = try {
        db.getInRatingRange(min, max)?.let { adapter.adapt(it) }?.also { puzzle ->
            Timber.d("Loaded puzzle in range [%d, %d], id=%d", min, max, puzzle.id)
            recordPuzzle(puzzle)
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load puzzle in rating range [%d, %d]", min, max)
        throw e
    }

    private fun recordPuzzle(puzzle: Puzzle) {
        LastLoadedPuzzleLog.record(id = puzzle.id, rating = puzzle.rating)
    }
}
