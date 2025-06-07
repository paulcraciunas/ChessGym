package com.paulcraciunas.data.impl

import androidx.room.Database
import androidx.room.RoomDatabase
import com.paulcraciunas.data.db.Puzzle
import com.paulcraciunas.data.db.PuzzleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [Puzzle::class], version = 1)
abstract class PuzzleDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao

    suspend fun insert(puzzle: Puzzle) = withContext(Dispatchers.IO) { puzzleDao().insert(puzzle) }

    suspend fun bulkInsert(all: List<Puzzle>) = withContext(Dispatchers.IO) { puzzleDao().insertAll(all) }

    suspend fun get(count: Int): List<Puzzle> = withContext(Dispatchers.IO) { puzzleDao().get(count) }

    suspend fun getByRating(rating: Int): Puzzle? = withContext(Dispatchers.IO) { puzzleDao().getByRating(rating) }

    suspend fun getInRatingRange(min: Int, max: Int): Puzzle? = withContext(Dispatchers.IO) { puzzleDao().getInRange(min, max) }
}
