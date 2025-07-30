package com.paulcraciunas.domain.api

import java.time.LocalDateTime

data class PuzzleCompletionResult(
    val puzzleId: String,
    val puzzleRating: Int,
    val wasSuccessful: Boolean,
    val ratingChange: Int,
    val timeSpentSeconds: Long,
    val completedAt: LocalDateTime = LocalDateTime.now()
)
