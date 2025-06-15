package com.paulcraciunas.puzzles.impl.impl

import androidx.room.Database
import androidx.room.RoomDatabase
import com.paulcraciunas.puzzles.impl.db.Puzzle
import com.paulcraciunas.puzzles.impl.db.PuzzleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [Puzzle::class], version = 1)
internal abstract class AbstractPuzzleDatabase : RoomDatabase(), PuzzleDatabase {
    abstract fun puzzleDao(): PuzzleDao

    override suspend fun insert(puzzle: Puzzle) = withContext(Dispatchers.IO) { puzzleDao().insert(puzzle) }

    override suspend fun bulkInsert(all: List<Puzzle>) = withContext(Dispatchers.IO) { puzzleDao().insertAll(all) }

    override suspend fun get(count: Int): List<Puzzle> = withContext(Dispatchers.IO) { puzzleDao().get(count) }

    override suspend fun getByRating(rating: Int): Puzzle? = withContext(Dispatchers.IO) { puzzleDao().getByRating(rating) }

    override suspend fun getInRatingRange(min: Int, max: Int): Puzzle? = withContext(Dispatchers.IO) { puzzleDao().getInRange(min, max) }
}
