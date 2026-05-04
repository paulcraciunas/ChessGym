package com.paulcraciunas.chessgym.services

import com.paulcraciunas.chessgym.models.AchievementStatistic
import com.paulcraciunas.chessgym.models.AchievementStatisticsResponse
import com.paulcraciunas.chessgym.repositories.UserRepository

class DefaultStatisticsService(
    private val userRepository: UserRepository,
) : StatisticsService {

    override suspend fun computeAchievementStatistics(): AchievementStatisticsResponse {
        val allUsers = userRepository.findAll()
        val totalUsers = allUsers.size.toLong()

        if (totalUsers == 0L) {
            return AchievementStatisticsResponse(
                totalUsers = 0,
                achievements = emptyList(),
            )
        }

        val statistics = mutableListOf<AchievementStatistic>()

        val achievementCounts = mutableMapOf<String, MutableMap<Int, Int>>()
        for (user in allUsers) {
            for ((achievementId, progressValue) in user.achievements.progress) {
                val tierThresholds = ACHIEVEMENT_TIERS[achievementId] ?: continue
                val reachedTier = tierThresholds.indexOfLast { progressValue >= it } + 1
                if (reachedTier > 0) {
                    val tiers = achievementCounts.getOrPut(achievementId) { mutableMapOf() }
                    for (tier in 1..reachedTier) {
                        tiers[tier] = (tiers[tier] ?: 0) + 1
                    }
                }
            }
        }

        for ((achievementId, tiers) in achievementCounts) {
            for ((tier, count) in tiers) {
                statistics.add(
                    AchievementStatistic(
                        achievementId = achievementId,
                        tier = tier,
                        percentageOfUsers = (count.toDouble() / totalUsers) * 100.0,
                    )
                )
            }
        }

        return AchievementStatisticsResponse(
            totalUsers = totalUsers,
            achievements = statistics.sortedWith(
                compareBy({ it.achievementId }, { it.tier })
            ),
        )
    }

    companion object {
        // Mirrors Achievement enum thresholds from the Android domain layer
        val ACHIEVEMENT_TIERS: Map<String, List<Long>> = mapOf(
            "RATED_PUZZLES_SOLVED" to listOf(50, 200, 500, 1000, 2000),
            "PUZZLE_RUSH_SESSIONS" to listOf(10, 50, 100, 250, 500),
            "STREAK_SESSIONS" to listOf(10, 50, 100, 250, 500),
            "FAILED_PUZZLES_REDEEMED" to listOf(25, 100, 250, 500, 1000),
            "FIND_SQUARE_SESSIONS" to listOf(10, 50, 100, 250, 500),
            "MOVE_PIECE_SESSIONS" to listOf(10, 50, 100, 250, 500),
            "BLIND_MODE_WINS" to listOf(5, 25, 50, 100, 200),
            "RATED_WIN_STREAK" to listOf(3, 5, 10, 15, 25),
            "RATING_CLIMBER" to listOf(1100, 1300, 1500, 1800, 2000),
            "BLIND_STRATEGIST" to listOf(500, 700, 900, 1100, 1300),
            "RUSH_CHAMPION" to listOf(10, 20, 30, 40, 50),
            "STREAK_LEGEND" to listOf(10, 25, 50, 75, 100),
            "EAGLE_EYE" to listOf(50, 100, 200, 350, 500),
            "KNIGHTS_PATH" to listOf(50, 100, 200, 350, 500),
            "PUZZLE_ADDICT" to listOf(100, 500, 1000, 2500, 5000),
            "TIME_INVESTED" to listOf(3_600_000, 18_000_000, 36_000_000, 72_000_000, 180_000_000),
            "DAILY_GRINDER" to listOf(3, 7, 14, 30, 60),
            "CONSISTENCY_KING" to listOf(7, 14, 30, 60, 100),
            "BOARD_VISION" to listOf(25, 75, 150, 300, 500),
            "RUSH_SOLVER" to listOf(50, 200, 500, 1000, 2000),
        )
    }
}
