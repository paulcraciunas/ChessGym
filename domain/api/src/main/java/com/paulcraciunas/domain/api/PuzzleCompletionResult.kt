package com.paulcraciunas.domain.api

data class PuzzleCompletionResult(
    val puzzleId: Int?,
    val puzzleRating: Int,
    val wasSuccessful: Boolean,
    val ratingChange: Int, // Always positive
    val timeSpentMillis: Long,
)
