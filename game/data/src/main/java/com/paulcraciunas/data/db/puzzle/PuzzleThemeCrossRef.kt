package com.paulcraciunas.data.db.puzzle

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["puzzleId", "themeId"])
data class PuzzleThemeCrossRef(
    val puzzleId: Int,
    @ColumnInfo(index = true)
    val themeId: Int
)
