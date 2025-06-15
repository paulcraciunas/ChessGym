package com.paulcraciunas.puzzles.impl.impl

import java.util.concurrent.ThreadLocalRandom

/**
 * Uses ThreadLocalRandom to generate random numbers
 */
class TLRandomFactory : RandomFactory {
    override fun nextInt(from: Int, to: Int): Int = ThreadLocalRandom.current().nextInt(from, to)
}
