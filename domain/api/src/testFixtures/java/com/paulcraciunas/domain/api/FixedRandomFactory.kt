package com.paulcraciunas.domain.api

class FixedRandomFactory(var returnValue: Int = 0) : RandomFactory {
    override fun nextInt(from: Int, to: Int): Int = returnValue
}
