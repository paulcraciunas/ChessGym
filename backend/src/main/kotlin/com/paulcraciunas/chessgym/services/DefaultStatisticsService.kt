package com.paulcraciunas.chessgym.services

import com.paulcraciunas.chessgym.models.AchievementStatistic
import com.paulcraciunas.chessgym.models.AchievementStatisticsResponse
import com.paulcraciunas.chessgym.models.UserDto
import com.paulcraciunas.chessgym.repositories.UserRepository
import java.time.Instant

class DefaultStatisticsService(
    private val userRepository: UserRepository,
    private val cacheTtlSeconds: Long = DEFAULT_CACHE_TTL_SECONDS,
) : StatisticsService {

    @Volatile
    private var cachedResponse: AchievementStatisticsResponse? = null

    @Volatile
    private var cacheTimestamp: Instant = Instant.EPOCH

    override suspend fun computeAchievementStatistics(): AchievementStatisticsResponse {
        val cached = cachedResponse
        if (cached != null && Instant.now().isBefore(cacheTimestamp.plusSeconds(cacheTtlSeconds))) {
            return cached
        }

        val allUsers = userRepository.findAll()
        val result = computeFrom(allUsers)

        cachedResponse = result
        cacheTimestamp = Instant.now()

        return result
    }

    private fun computeFrom(allUsers: List<UserDto>): AchievementStatisticsResponse {
        val totalUsers = allUsers.size.toLong()

        if (totalUsers == 0L) {
            return AchievementStatisticsResponse(
                totalUsers = 0,
                achievements = emptyList(),
            )
        }

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

        val statistics = mutableListOf<AchievementStatistic>()
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
        private const val DEFAULT_CACHE_TTL_SECONDS: Long = 3600 // 1 hour

        // Mirrors Achievement enum thresholds from the Android domain layer
        val ACHIEVEMENT_TIERS: Map<String, List<Long>> = mapOf(
            "RATED_PUZZLES_SOLVED" to listOf(5, 25, 100, 250, 1000),
            "PUZZLE_RUSH_SESSIONS" to listOf(5, 25, 100, 250, 1000),
            "STREAK_SESSIONS" to listOf(5, 10, 25, 50, 100),
            "FAILED_PUZZLES_REDEEMED" to listOf(5, 25, 100, 250, 500),
            "FIND_SQUARE_SESSIONS" to listOf(5, 25, 100, 250, 500),
            "KNIGHT_PATH_SESSIONS" to listOf(5, 25, 100, 250, 500),
            "BLIND_MODE_WINS" to listOf(1, 5, 10, 25, 50),
            "RATED_WIN_STREAK" to listOf(3, 5, 10, 15, 25),
            "RATING_CLIMBER" to listOf(1300, 1500, 1750, 2000, 2250),
            "BLIND_STRATEGIST" to listOf(500, 700, 1000, 1300, 1600),
            "RUSH_CHAMPION" to listOf(10, 20, 30, 50, 75),
            "STREAK_LEGEND" to listOf(5, 10, 25, 50, 100),
            "EAGLE_EYE" to listOf(5, 10, 15, 25, 50),
            "KNIGHTS_PATH" to listOf(5, 7, 10, 15, 20),
            "PUZZLE_ADDICT" to listOf(50, 250, 500, 1000, 2500),
            "TIME_INVESTED" to listOf(1, 10, 50, 100, 500),
            "DAILY_GRINDER" to listOf(3, 7, 21, 60, 240),
            "CONSISTENCY_KING" to listOf(3, 7, 14, 30, 100),
            "BOARD_VISION" to listOf(10, 50, 200, 500, 2000),
            "RUSH_SOLVER" to listOf(25, 100, 250, 1000, 5000),
        )
    }
}
