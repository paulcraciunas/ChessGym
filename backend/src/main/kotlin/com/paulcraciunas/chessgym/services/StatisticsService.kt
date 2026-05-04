package com.paulcraciunas.chessgym.services

import com.paulcraciunas.chessgym.models.AchievementStatisticsResponse

interface StatisticsService {
    suspend fun computeAchievementStatistics(): AchievementStatisticsResponse
}
