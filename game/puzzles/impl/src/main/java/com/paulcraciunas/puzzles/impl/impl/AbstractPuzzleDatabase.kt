package com.paulcraciunas.puzzles.impl.impl

// TODO(https://github.com/paulcraciunas/ChessGym/issues/65) Paul: Extract this implementation to a separate module
//noinspection PureDomain
import androidx.room.Database
//noinspection PureDomain
import androidx.room.RoomDatabase
import com.paulcraciunas.puzzles.impl.db.Puzzle
import com.paulcraciunas.puzzles.impl.db.PuzzleDao

@Database(entities = [Puzzle::class], version = 1)
abstract class AbstractPuzzleDatabase : RoomDatabase(), PuzzleDatabase {
    abstract fun puzzleDao(): PuzzleDao

    override suspend fun insert(puzzle: Puzzle) = puzzleDao().insert(puzzle)

    override suspend fun bulkInsert(all: List<Puzzle>) = puzzleDao().insertAll(all)

    override suspend fun get(count: Int): List<Puzzle> = puzzleDao().get(count)

    override suspend fun getById(id: Int): Puzzle? = puzzleDao().getById(id)

    override suspend fun getByRating(rating: Int): Puzzle? = puzzleDao().getByRating(rating)

    override suspend fun getInRatingRange(min: Int, max: Int): Puzzle? = puzzleDao().getInRange(min, max)
}
