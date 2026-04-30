package com.paulcraciunas.domain.impl.achievements

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.AchievementNotification
import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.user.api.User
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class UpdateAchievementProgressImpl @Inject constructor(
    private val notificationManager: AchievementNotificationManager,
    private val clock: Clock = Clock.systemDefaultZone(),
) : UpdateAchievementProgress {

    override suspend fun invoke(user: User): User {
        val today = LocalDate.now(clock)
        val achievements = user.achievements
        val dailyStreak = computeDailyStreak(achievements, today)
        val updatedProgress = buildProgressMap(user, dailyStreak)
        val cachedProgress = achievements.progress
        val newNotifications = mutableListOf<AchievementNotification>()
        val updatedUnseen = achievements.unseenAchievements.toMutableSet()

        for (achievement in Achievement.entries) {
            val oldVal = cachedProgress[achievement.name] ?: 0L
            val newVal = updatedProgress[achievement.name] ?: 0L
            val unlockedTier = achievement.highestNewTier(oldVal, newVal)

            if (unlockedTier != null) {
                updatedUnseen.add(achievement.name)
                newNotifications.add(AchievementNotification(achievement, unlockedTier))
            }
        }

        // Emitted before persistence -- benign if the caller fails to save, since the
        // cached progress won't update and the same tier change will re-trigger next time.
        newNotifications.forEach { notificationManager.emit(it) }

        return user.copy(
            achievements = achievements.copy(
                progress = updatedProgress,
                unseenAchievements = updatedUnseen,
                lastActiveDate = dailyStreak.lastActiveDate,
                consecutiveDaysStreak = dailyStreak.consecutiveDaysStreak,
                bestConsecutiveDaysStreak = dailyStreak.bestConsecutiveDaysStreak,
            )
        )
    }

    private fun buildProgressMap(user: User, dailyStreak: DailyStreakResult): Map<String, Long> {
        val stats = user.statistics
        val highScores = user.highScores
        val achievements = user.achievements

        return Achievement.entries.associate { achievement ->
            val value = when (achievement) {
                Achievement.RATED_PUZZLES_SOLVED -> stats.ratedPuzzlesSolved.toLong()
                Achievement.PUZZLE_RUSH_SESSIONS -> stats.puzzleRushSessions.toLong()
                Achievement.STREAK_SESSIONS -> stats.streakSessions.toLong()
                Achievement.FAILED_PUZZLES_REDEEMED -> stats.failedPuzzlesRedeemed.toLong()
                Achievement.FIND_SQUARE_SESSIONS -> stats.findSquareSessions.toLong()
                Achievement.MOVE_PIECE_SESSIONS -> stats.moveThePieceSessions.toLong()
                Achievement.BLIND_MODE_WINS -> stats.blindModeWins.toLong()
                Achievement.BOARD_VISION -> (stats.findSquareSessions + stats.moveThePieceSessions).toLong()
                Achievement.RUSH_SOLVER -> stats.rushPuzzlesSolved.toLong()
                Achievement.RATING_CLIMBER -> highScores.ratedPuzzle.toLong()
                Achievement.BLIND_STRATEGIST -> user.ratings.blindMode.toLong()
                Achievement.RUSH_CHAMPION -> highScores.puzzleRush.toLong()
                Achievement.STREAK_LEGEND -> highScores.puzzleStreak.toLong()
                Achievement.EAGLE_EYE -> highScores.findTheSquare.toLong()
                Achievement.KNIGHTS_PATH -> highScores.moveThePiece.toLong()
                Achievement.PUZZLE_ADDICT -> stats.puzzlesSolved.toLong()
                Achievement.TIME_INVESTED -> stats.totalTimeSpent / MILLIS_PER_HOUR
                Achievement.RATED_WIN_STREAK -> achievements.bestRatedWinStreak.toLong()
                Achievement.DAILY_GRINDER -> dailyStreak.consecutiveDaysStreak.toLong()
                Achievement.CONSISTENCY_KING -> dailyStreak.bestConsecutiveDaysStreak.toLong()
            }
            achievement.name to value
        }
    }

    private fun computeDailyStreak(achievements: User.Achievements, today: LocalDate): DailyStreakResult {
        val lastDate = achievements.lastActiveDate
        val currentStreak = achievements.consecutiveDaysStreak
        val bestStreak = achievements.bestConsecutiveDaysStreak

        return when {
            lastDate == null || lastDate.isBefore(today.minusDays(1)) -> {
                DailyStreakResult(
                    lastActiveDate = today,
                    consecutiveDaysStreak = 1,
                    bestConsecutiveDaysStreak = maxOf(bestStreak, 1),
                )
            }
            lastDate == today.minusDays(1) -> {
                val newStreak = currentStreak + 1
                DailyStreakResult(
                    lastActiveDate = today,
                    consecutiveDaysStreak = newStreak,
                    bestConsecutiveDaysStreak = maxOf(bestStreak, newStreak),
                )
            }
            else -> {
                DailyStreakResult(
                    lastActiveDate = today,
                    consecutiveDaysStreak = currentStreak,
                    bestConsecutiveDaysStreak = bestStreak,
                )
            }
        }
    }

    private data class DailyStreakResult(
        val lastActiveDate: LocalDate,
        val consecutiveDaysStreak: Int,
        val bestConsecutiveDaysStreak: Int,
    )

    companion object {
        private const val MILLIS_PER_HOUR = 3_600_000L
    }
}
