package com.paulcraciunas.domain

//TODO Paul: This is only temporary. Delete this and reimplement it properly
data class Puzzle(
    val binary: ByteArray,
    val rating: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Puzzle

        if (rating != other.rating) return false
        if (!binary.contentEquals(other.binary)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rating
        result = 31 * result + binary.contentHashCode()
        return result
    }
}
