package com.paulcraciunas.data.db.puzzle

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PuzzleDao {
    @Insert
    suspend fun insert(puzzle: Puzzle): Long

    @Insert
    suspend fun insertCrossRef(crossRef: PuzzleThemeCrossRef)

    @Transaction
    @Query("SELECT * FROM Puzzles WHERE ratingId = :targetRating ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPuzzleByRating(targetRating: Int): PuzzleWithThemes?

    @Transaction
    @Query("SELECT * FROM Puzzles ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomPuzzles(count: Int): List<PuzzleWithThemes>

    @Transaction
    @Query("SELECT * FROM Puzzles WHERE id IN (SELECT puzzleId FROM PuzzleThemeCrossRef WHERE themeId IN (SELECT id FROM Themes WHERE theme = :theme)) ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomPuzzlesByTheme(theme: String, count: Int): List<PuzzleWithThemes>
}
