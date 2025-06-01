package com.paulcraciunas.data.db.theme

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ThemeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(theme: Theme): Long

    @Query("SELECT * FROM Themes WHERE theme = :name LIMIT 1")
    suspend fun getByName(name: String): Theme?

    @Query("SELECT * FROM Themes")
    suspend fun getAll(): List<Theme>
}