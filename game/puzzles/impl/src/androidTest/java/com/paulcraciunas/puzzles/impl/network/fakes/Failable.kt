package com.paulcraciunas.puzzles.impl.network.fakes

internal abstract class Failable {
    private var exception: Throwable? = null

    fun fail(error: Throwable) = apply { exception = error }
    
    fun reset() = apply { exception = null }

    protected fun check() = exception?.let { throw it }
}
