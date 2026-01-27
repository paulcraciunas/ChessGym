package com.paulcraciunas.domain.api

/**
 * A simple sequential random factory for testing.
 * Returns predictable values for reproducible tests.
 */
class SequentialRandomFactory : RandomFactory {
    private var counter = 0

    override fun nextInt(from: Int, to: Int): Int {
        if (from >= to) throw IllegalArgumentException("incorrect bounds. from: $from must be strictly smaller than to: $to")
        val range = to - from
        val result = from + (counter % range)
        counter++
        return result
    }
}
