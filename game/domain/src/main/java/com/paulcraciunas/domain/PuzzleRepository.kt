package com.paulcraciunas.domain

interface PuzzleRepository {
    suspend fun get(count: Int): List<Puzzle>
    suspend fun getByRating(targetRating: Int): Puzzle?
    suspend fun getByRatingRange(min: Int, max: Int): Puzzle?
    suspend fun getByIncreasingRatings(count: Int = COUNT, increment: Int = INCREMENT, from: Int = RATING_START): List<Puzzle>

    companion object Defaults {
        const val RATING_START = 400
        const val INCREMENT = 40
        const val COUNT = 100
    }
}
