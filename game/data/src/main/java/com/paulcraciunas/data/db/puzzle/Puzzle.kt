package com.paulcraciunas.data.db.puzzle

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.paulcraciunas.data.db.rating.Rating

@Entity(
    tableName = "Puzzles",
    foreignKeys = [ForeignKey(
        entity = Rating::class,
        parentColumns = ["value"],
        childColumns = ["ratingId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Puzzle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fen: String,
    val moves: String,
    val ratingId: Int
)
