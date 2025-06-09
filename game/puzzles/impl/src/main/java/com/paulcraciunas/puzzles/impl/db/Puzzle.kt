package com.paulcraciunas.puzzles.impl.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["rating"]), Index(value = ["fenBinary"])])
data class Puzzle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fenBinary: ByteArray,
    val rating: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return (id == (other as Puzzle).id)
    }

    override fun hashCode(): Int = id
}
