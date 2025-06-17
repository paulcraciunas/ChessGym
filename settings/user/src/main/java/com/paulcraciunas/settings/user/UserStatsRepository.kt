package com.paulcraciunas.settings.user

import kotlinx.coroutines.flow.Flow

interface UserStatsRepository {
    val userStats: Flow<UserStats>

    suspend fun updatePuzzlesPlayed(count: Int)
    suspend fun updatePuzzlesSolved(count: Int)
    suspend fun updateCurrentRating(rating: Int)
    suspend fun updateBestPuzzleRushScore(score: Int)
    suspend fun updateBestBlindModeScore(score: Int)
    suspend fun updateBestVisualizationScore(score: Int)
}
