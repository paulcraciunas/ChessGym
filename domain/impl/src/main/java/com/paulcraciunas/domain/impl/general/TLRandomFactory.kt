package com.paulcraciunas.domain.impl.general

import com.paulcraciunas.domain.api.general.RandomFactory
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject

/**
 * Uses ThreadLocalRandom to generate random numbers
 */
class TLRandomFactory @Inject constructor() : RandomFactory {
    override fun nextInt(from: Int, to: Int): Int = ThreadLocalRandom.current().nextInt(from, to)
}
