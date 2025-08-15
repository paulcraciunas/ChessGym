package com.paulcraciunas.domain.api

data class PuzzleCompletionResult(
    val puzzleRating: Int,
    val wasSuccessful: Boolean,
    val ratingChange: Int,
    val timeSpentMillis: Long,
)
