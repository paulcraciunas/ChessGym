package com.paulcraciunas.data.db.puzzle

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.paulcraciunas.data.db.theme.Theme

data class PuzzleWithThemes(
    @Embedded val puzzle: Puzzle,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PuzzleThemeCrossRef::class,
            parentColumn = "puzzleId",
            entityColumn = "themeId"
        )
    ) val themes: List<Theme>
)
