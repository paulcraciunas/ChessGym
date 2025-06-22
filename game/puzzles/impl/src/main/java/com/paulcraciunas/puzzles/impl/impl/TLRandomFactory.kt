package com.paulcraciunas.puzzles.impl.impl

import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject

/**
 * Uses ThreadLocalRandom to generate random numbers
 */
class TLRandomFactory @Inject constructor() : RandomFactory {
    override fun nextInt(from: Int, to: Int): Int = ThreadLocalRandom.current().nextInt(from, to)
}
