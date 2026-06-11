package com.paulcraciunas.domain.api.boardvis

interface OnKnightPathComplete {
    suspend operator fun invoke(result: KnightPathResult)
}

data class KnightPathResult(
    val score: Int,
    val timeSpentMillis: Long,
)
