package com.paulcraciunas.data.db.rating

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RatingDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rating: Rating): Long

    @Query("SELECT * FROM Ratings WHERE value = :value LIMIT 1")
    suspend fun get(value: Int): Rating?
}