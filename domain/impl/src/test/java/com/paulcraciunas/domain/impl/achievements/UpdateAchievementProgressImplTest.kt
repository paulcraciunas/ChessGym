package com.paulcraciunas.domain.impl.achievements

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.FakeAchievementNotificationManager
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

internal class UpdateAchievementProgressImplTest {
    private val fixedDate = LocalDate.of(2026, 4, 30)
    private val fixedClock = Clock.fixed(
        fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault(),
    )
    private val notificationManager = FakeAchievementNotificationManager()
    private val underTest = UpdateAchievementProgressImpl(notificationManager, fixedClock)

    @Nested
    internal inner class TierDetection {
        @ParameterizedTest
        @EnumSource(Achievement::class)
        fun `GIVEN no prior progress WHEN stat reaches tier 1 THEN unlocks tier 1`(achievement: Achievement) = runTest {
            val user = userWithStat(achievement, tierValue = 1)

            val result = underTest(user)

            assertEquals(Achievement.Tier.ONE, achievement.tierFrom(result.progress(achievement)))
            assertTrue(result.achievements.unseenAchievements.contains(achievement.name))
            val notification = notificationManager.emitted.find { it.achievement == achievement }
            assertNotNull(notification)
            assertEquals(Achievement.Tier.ONE, notification!!.tier)
        }

        @ParameterizedTest
        @EnumSource(Achievement::class)
        fun `GIVEN tier 1 cached WHEN stat reaches tier 2 THEN unlocks tier 2`(achievement: Achievement) = runTest {
            val tier1Value = statValueForTier(achievement, Achievement.Tier.ONE)
            val user = userWithStat(achievement, tierValue = 2).withCachedProgress(
                achievement, tier1Value,
            )

            val result = underTest(user)

            assertEquals(Achievement.Tier.TWO, achievement.tierFrom(result.progress(achievement)))
            assertTrue(result.achievements.unseenAchievements.contains(achievement.name))
        }

        @ParameterizedTest
        @EnumSource(Achievement::class)
        fun `GIVEN tier 1 cached WHEN stat unchanged THEN no notification emitted`(achievement: Achievement) = runTest {
            val tier1Value = statValueForTier(achievement, Achievement.Tier.ONE)
            val user = userWithStat(achievement, tierValue = 1).withCachedProgress(
                achievement, tier1Value,
            )

            val result = underTest(user)

            assertEquals(Achievement.Tier.ONE, achievement.tierFrom(result.progress(achievement)))
            val notification = notificationManager.emitted.find { it.achievement == achievement }
            assertNull(notification)
        }

        @Test
        fun `GIVEN no progress WHEN stat skips to tier 3 THEN emits only highest tier notification`() = runTest {
            val user = User(
                statistics = User.Statistics(ratedPuzzlesSolved = 100),
            )

            val result = underTest(user)

            val notifications = notificationManager.emitted.filter {
                it.achievement == Achievement.RATED_PUZZLES_SOLVED
            }
            assertEquals(1, notifications.size)
            assertEquals(Achievement.Tier.THREE, notifications[0].tier)
            assertTrue(result.achievements.unseenAchievements.contains("RATED_PUZZLES_SOLVED"))
        }

        @ParameterizedTest
        @EnumSource(Achievement::class)
        fun `GIVEN max tier cached WHEN stat unchanged THEN no notification`(achievement: Achievement) = runTest {
            val maxValue = statValueForTier(achievement, Achievement.Tier.FIVE)
            val user = userWithStat(achievement, tierValue = 5).withCachedProgress(
                achievement, maxValue,
            )

            val result = underTest(user)

            assertEquals(Achievement.Tier.FIVE, achievement.tierFrom(result.progress(achievement)))
            val notification = notificationManager.emitted.find { it.achievement == achievement }
            assertNull(notification)
        }
    }

    @Nested
    internal inner class ProgressDerivation {
        @Test
        fun `GIVEN rated puzzles solved WHEN invoke THEN derives from statistics`() = runTest {
            val user = User(statistics = User.Statistics(ratedPuzzlesSolved = 30))

            val result = underTest(user)

            assertEquals(30L, result.progress(Achievement.RATED_PUZZLES_SOLVED))
        }

        @Test
        fun `GIVEN puzzle rush sessions WHEN invoke THEN derives from statistics`() = runTest {
            val user = User(statistics = User.Statistics(puzzleRushSessions = 10))

            val result = underTest(user)

            assertEquals(10L, result.progress(Achievement.PUZZLE_RUSH_SESSIONS))
        }

        @Test
        fun `GIVEN streak sessions WHEN invoke THEN derives from statistics`() = runTest {
            val user = User(statistics = User.Statistics(streakSessions = 7))

            val result = underTest(user)

            assertEquals(7L, result.progress(Achievement.STREAK_SESSIONS))
        }

        @Test
        fun `GIVEN failed puzzles redeemed WHEN invoke THEN derives from statistics`() = runTest {
            val user = User(statistics = User.Statistics(failedPuzzlesRedeemed = 12))

            val result = underTest(user)

            assertEquals(12L, result.progress(Achievement.FAILED_PUZZLES_REDEEMED))
        }

        @Test
        fun `GIVEN find square sessions WHEN invoke THEN derives from statistics`() = runTest {
            val user = User(statistics = User.Statistics(findSquareSessions = 20))

            val result = underTest(user)

            assertEquals(20L, result.progress(Achievement.FIND_SQUARE_SESSIONS))
        }

        @Test
        fun `GIVEN move piece sessions WHEN invoke THEN derives from statistics`() = runTest {
            val user = User(statistics = User.Statistics(knightPathSessions = 15))

            val result = underTest(user)

            assertEquals(15L, result.progress(Achievement.KNIGHT_PATH_SESSIONS))
        }

        @Test
        fun `GIVEN blind mode wins WHEN invoke THEN derives from statistics`() = runTest {
            val user = User(statistics = User.Statistics(blindModeWins = 8))

            val result = underTest(user)

            assertEquals(8L, result.progress(Achievement.BLIND_MODE_WINS))
        }

        @Test
        fun `GIVEN board viz sessions WHEN invoke THEN derives from sum of find and move`() = runTest {
            val user = User(
                statistics = User.Statistics(findSquareSessions = 5, knightPathSessions = 7),
            )

            val result = underTest(user)

            assertEquals(12L, result.progress(Achievement.BOARD_VISION))
        }

        @Test
        fun `GIVEN rush puzzles solved WHEN invoke THEN derives from statistics`() = runTest {
            val user = User(statistics = User.Statistics(rushPuzzlesSolved = 50))

            val result = underTest(user)

            assertEquals(50L, result.progress(Achievement.RUSH_SOLVER))
        }

        @Test
        fun `GIVEN rating climber WHEN invoke THEN derives from high scores`() = runTest {
            val user = User(highScores = User.HighScores(ratedPuzzle = 1500))

            val result = underTest(user)

            assertEquals(1500L, result.progress(Achievement.RATING_CLIMBER))
        }

        @Test
        fun `GIVEN blind strategist WHEN invoke THEN derives from blind mode rating`() = runTest {
            val user = User(ratings = User.Ratings(blindMode = 700))

            val result = underTest(user)

            assertEquals(700L, result.progress(Achievement.BLIND_STRATEGIST))
        }

        @Test
        fun `GIVEN rush champion WHEN invoke THEN derives from high scores`() = runTest {
            val user = User(highScores = User.HighScores(puzzleRush = 25))

            val result = underTest(user)

            assertEquals(25L, result.progress(Achievement.RUSH_CHAMPION))
        }

        @Test
        fun `GIVEN streak legend WHEN invoke THEN derives from high scores`() = runTest {
            val user = User(highScores = User.HighScores(puzzleStreak = 30))

            val result = underTest(user)

            assertEquals(30L, result.progress(Achievement.STREAK_LEGEND))
        }

        @Test
        fun `GIVEN eagle eye WHEN invoke THEN derives from high scores`() = runTest {
            val user = User(highScores = User.HighScores(findTheSquare = 18))

            val result = underTest(user)

            assertEquals(18L, result.progress(Achievement.EAGLE_EYE))
        }

        @Test
        fun `GIVEN knights path WHEN invoke THEN derives from high scores`() = runTest {
            val user = User(highScores = User.HighScores(knightPath = 12))

            val result = underTest(user)

            assertEquals(12L, result.progress(Achievement.KNIGHTS_PATH))
        }

        @Test
        fun `GIVEN puzzle addict WHEN invoke THEN derives from total puzzles solved`() = runTest {
            val user = User(statistics = User.Statistics(puzzlesSolved = 300))

            val result = underTest(user)

            assertEquals(300L, result.progress(Achievement.PUZZLE_ADDICT))
        }

        @Test
        fun `GIVEN time invested WHEN invoke THEN derives from total time in hours`() = runTest {
            val user = User(statistics = User.Statistics(totalTimeSpent = 7_200_000L))

            val result = underTest(user)

            assertEquals(2L, result.progress(Achievement.TIME_INVESTED))
        }

        @Test
        fun `GIVEN rated win streak WHEN invoke THEN derives from best win streak`() = runTest {
            val user = User(achievements = User.Achievements(bestRatedWinStreak = 10))

            val result = underTest(user)

            assertEquals(10L, result.progress(Achievement.RATED_WIN_STREAK))
        }
    }

    @Nested
    internal inner class DailyStreak {
        @Test
        fun `GIVEN no prior active date WHEN invoke THEN sets streak to 1`() = runTest {
            val user = User()

            val result = underTest(user)

            assertEquals(fixedDate, result.achievements.lastActiveDate)
            assertEquals(1, result.achievements.consecutiveDaysStreak)
            assertEquals(1, result.achievements.bestConsecutiveDaysStreak)
        }

        @Test
        fun `GIVEN active yesterday WHEN invoke THEN increments streak`() = runTest {
            val user = User(
                achievements = User.Achievements(
                    lastActiveDate = fixedDate.minusDays(1),
                    consecutiveDaysStreak = 5,
                    bestConsecutiveDaysStreak = 10,
                ),
            )

            val result = underTest(user)

            assertEquals(fixedDate, result.achievements.lastActiveDate)
            assertEquals(6, result.achievements.consecutiveDaysStreak)
            assertEquals(10, result.achievements.bestConsecutiveDaysStreak)
        }

        @Test
        fun `GIVEN active 3 days ago WHEN invoke THEN resets streak to 1`() = runTest {
            val user = User(
                achievements = User.Achievements(
                    lastActiveDate = fixedDate.minusDays(3),
                    consecutiveDaysStreak = 15,
                    bestConsecutiveDaysStreak = 20,
                ),
            )

            val result = underTest(user)

            assertEquals(fixedDate, result.achievements.lastActiveDate)
            assertEquals(1, result.achievements.consecutiveDaysStreak)
            assertEquals(20, result.achievements.bestConsecutiveDaysStreak)
        }

        @Test
        fun `GIVEN active today already WHEN invoke again THEN does not change streak`() = runTest {
            val user = User(
                achievements = User.Achievements(
                    lastActiveDate = fixedDate,
                    consecutiveDaysStreak = 5,
                    bestConsecutiveDaysStreak = 10,
                ),
            )

            val result = underTest(user)

            assertEquals(fixedDate, result.achievements.lastActiveDate)
            assertEquals(5, result.achievements.consecutiveDaysStreak)
            assertEquals(10, result.achievements.bestConsecutiveDaysStreak)
        }

        @Test
        fun `GIVEN streak beats best WHEN invoke THEN updates bestConsecutiveDaysStreak`() = runTest {
            val user = User(
                achievements = User.Achievements(
                    lastActiveDate = fixedDate.minusDays(1),
                    consecutiveDaysStreak = 10,
                    bestConsecutiveDaysStreak = 10,
                ),
            )

            val result = underTest(user)

            assertEquals(11, result.achievements.consecutiveDaysStreak)
            assertEquals(11, result.achievements.bestConsecutiveDaysStreak)
        }

        @Test
        fun `GIVEN daily grinder at streak threshold WHEN invoke THEN unlocks achievement`() = runTest {
            val user = User(
                achievements = User.Achievements(
                    lastActiveDate = fixedDate.minusDays(1),
                    consecutiveDaysStreak = 2,
                    bestConsecutiveDaysStreak = 2,
                ),
            )

            val result = underTest(user)

            assertEquals(3L, result.progress(Achievement.DAILY_GRINDER))
            assertTrue(result.achievements.unseenAchievements.contains("DAILY_GRINDER"))
        }

        @Test
        fun `GIVEN consistency king at best streak threshold WHEN invoke THEN unlocks achievement`() = runTest {
            val user = User(
                achievements = User.Achievements(
                    lastActiveDate = fixedDate.minusDays(1),
                    consecutiveDaysStreak = 2,
                    bestConsecutiveDaysStreak = 2,
                ),
            )

            val result = underTest(user)

            assertEquals(3L, result.progress(Achievement.CONSISTENCY_KING))
            assertTrue(result.achievements.unseenAchievements.contains("CONSISTENCY_KING"))
        }
    }

    @Nested
    internal inner class CacheIntegrity {
        @Test
        fun `GIVEN default user WHEN invoke THEN populates progress cache for all achievements`() = runTest {
            val result = underTest(User())

            assertEquals(Achievement.entries.size, result.achievements.progress.size)
            assertNotNull(result.achievements.lastActiveDate)
        }

        @Test
        fun `GIVEN cached progress WHEN stat unchanged THEN cache values preserved`() = runTest {
            val user = User(
                statistics = User.Statistics(ratedPuzzlesSolved = 5),
                achievements = User.Achievements(
                    progress = mapOf(Achievement.RATED_PUZZLES_SOLVED.name to 5L),
                ),
            )

            val result = underTest(user)

            assertEquals(5L, result.progress(Achievement.RATED_PUZZLES_SOLVED))
        }
    }

    private fun User.progress(achievement: Achievement): Long =
        achievements.progress[achievement.name] ?: 0L

    private fun User.withCachedProgress(achievement: Achievement, value: Long): User = copy(
        achievements = achievements.copy(
            progress = achievements.progress + (achievement.name to value),
        ),
    )

    /**
     * Creates a user whose stats place the given [achievement] at the specified [tierValue] (1-5).
     * Uses the exact threshold value for that tier.
     */
    private fun userWithStat(achievement: Achievement, tierValue: Int): User {
        val tier = Achievement.Tier.entries[tierValue - 1]
        val value = statValueForTier(achievement, tier)
        return userWithRawStat(achievement, value)
    }

    private fun statValueForTier(achievement: Achievement, tier: Achievement.Tier): Long {
        val progress = achievement.nextTierProgress(
            if (tier == Achievement.Tier.ONE) null else Achievement.Tier.entries[tier.ordinal - 1]
        ) ?: error("No threshold for $achievement tier $tier")
        return progress
    }

    private fun userWithRawStat(achievement: Achievement, value: Long): User {
        val intVal = value.toInt()
        return when (achievement) {
            Achievement.RATED_PUZZLES_SOLVED -> User(statistics = User.Statistics(ratedPuzzlesSolved = intVal))
            Achievement.PUZZLE_RUSH_SESSIONS -> User(statistics = User.Statistics(puzzleRushSessions = intVal))
            Achievement.STREAK_SESSIONS -> User(statistics = User.Statistics(streakSessions = intVal))
            Achievement.FAILED_PUZZLES_REDEEMED -> User(statistics = User.Statistics(failedPuzzlesRedeemed = intVal))
            Achievement.FIND_SQUARE_SESSIONS -> User(statistics = User.Statistics(findSquareSessions = intVal))
            Achievement.KNIGHT_PATH_SESSIONS -> User(statistics = User.Statistics(knightPathSessions = intVal))
            Achievement.BLIND_MODE_WINS -> User(statistics = User.Statistics(blindModeWins = intVal))
            Achievement.BOARD_VISION -> User(statistics = User.Statistics(findSquareSessions = intVal))
            Achievement.RUSH_SOLVER -> User(statistics = User.Statistics(rushPuzzlesSolved = intVal))
            Achievement.RATING_CLIMBER -> User(highScores = User.HighScores(ratedPuzzle = intVal))
            Achievement.BLIND_STRATEGIST -> User(ratings = User.Ratings(blindMode = intVal))
            Achievement.RUSH_CHAMPION -> User(highScores = User.HighScores(puzzleRush = intVal))
            Achievement.STREAK_LEGEND -> User(highScores = User.HighScores(puzzleStreak = intVal))
            Achievement.EAGLE_EYE -> User(highScores = User.HighScores(findTheSquare = intVal))
            Achievement.KNIGHTS_PATH -> User(highScores = User.HighScores(knightPath = intVal))
            Achievement.PUZZLE_ADDICT -> User(statistics = User.Statistics(puzzlesSolved = intVal))
            Achievement.TIME_INVESTED -> User(statistics = User.Statistics(totalTimeSpent = value * MILLIS_PER_HOUR))
            Achievement.RATED_WIN_STREAK -> User(achievements = User.Achievements(bestRatedWinStreak = intVal))
            Achievement.DAILY_GRINDER -> User(
                achievements = User.Achievements(
                    lastActiveDate = fixedDate.minusDays(1),
                    consecutiveDaysStreak = intVal - 1,
                    bestConsecutiveDaysStreak = intVal - 1,
                ),
            )
            Achievement.CONSISTENCY_KING -> User(
                achievements = User.Achievements(
                    lastActiveDate = fixedDate.minusDays(1),
                    consecutiveDaysStreak = intVal - 1,
                    bestConsecutiveDaysStreak = intVal - 1,
                ),
            )
        }
    }

    companion object {
        private const val MILLIS_PER_HOUR = 3_600_000L
    }
}
