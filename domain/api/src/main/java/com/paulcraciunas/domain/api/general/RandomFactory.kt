package com.paulcraciunas.domain.api.general

interface RandomFactory {
    fun nextInt(from: Int, to: Int): Int
}