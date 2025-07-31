package com.paulcraciunas.domain.api

interface RandomFactory {
    fun nextInt(from: Int, to: Int): Int
}