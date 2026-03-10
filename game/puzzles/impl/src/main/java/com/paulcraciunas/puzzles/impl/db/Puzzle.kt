package com.paulcraciunas.puzzles.impl.db

//noinspection PureDomain
import androidx.room.Entity
//noinspection PureDomain
import androidx.room.Index
//noinspection PureDomain
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["rating"])])
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
