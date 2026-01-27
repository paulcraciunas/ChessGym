package com.paulcraciunas.domain.api.puzzles

data class PuzzleCompletionResult(
    val puzzleId: Int?,
    val puzzleRating: Int,
    val wasSuccessful: Boolean,
    val ratingChange: Int, // Always positive
    val timeSpentMillis: Long,
)
