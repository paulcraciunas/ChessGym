package com.paulcraciunas.domain.api.general

class FixedRandomFactory(var returnValue: Int = 0) : RandomFactory {
    override fun nextInt(from: Int, to: Int): Int = returnValue
}
