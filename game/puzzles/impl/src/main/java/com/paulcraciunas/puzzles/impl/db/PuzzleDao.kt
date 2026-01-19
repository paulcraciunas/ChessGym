package com.paulcraciunas.puzzles.impl.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PuzzleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(puzzle: Puzzle): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(puzzles: List<Puzzle>)

    @Query("SELECT * FROM Puzzle ORDER BY RANDOM() LIMIT :count")
    suspend fun get(count: Int): List<Puzzle>

    @Query("SELECT * FROM Puzzle WHERE id = :id")
    suspend fun getById(id: Int): Puzzle?

    @Query("SELECT * FROM Puzzle WHERE rating = :targetRating ORDER BY RANDOM() LIMIT 1")
    suspend fun getByRating(targetRating: Int): Puzzle?

    @Query("SELECT * FROM Puzzle WHERE rating BETWEEN :min AND :max ORDER BY RANDOM() LIMIT 1")
    suspend fun getInRange(min: Int, max: Int): Puzzle?
}
