package com.paulcraciunas.screens.achievements.vm

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.AchievementState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState as UiState

internal class AchievementsUiStateAdapterTest {
    private val underTest = AchievementsUiStateAdapter()

    private fun adapt(vararg states: AchievementState): AchievementsUiState =
        underTest.adapt(states.toList())

    private fun flatAchievements(result: AchievementsUiState): List<UiState> =
        result.categories.flatMap { it.achievements }

    private fun findInCategory(
        result: AchievementsUiState,
        category: AchievementCategory,
    ): AchievementsUiState.CategoryGroup? =
        result.categories.find { it.category == category }

    @Nested
    internal inner class TypeMapping {
        @Test
        fun `GIVEN max tier WHEN adapt THEN maps to Complete`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.FIVE,
                    currentProgress = 1000,
                    nextThreshold = null,
                )
            )

            val item = flatAchievements(result).single()
            assertInstanceOf(UiState.Complete::class.java, item)
            assertTrue(item.isCompleted())
            assertEquals(Achievement.Tier.FIVE, item.currentTier)
            assertEquals(1f, item.progress())
            assertTrue(item.hasProgress())
        }

        @Test
        fun `GIVEN tier but not max WHEN adapt THEN maps to Earned`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.TWO,
                    currentProgress = 47,
                    nextThreshold = 100,
                )
            )

            val item = flatAchievements(result).single()
            assertInstanceOf(UiState.Earned::class.java, item)
            assertFalse(item.isCompleted())
            assertEquals(Achievement.Tier.TWO, item.currentTier)
            assertEquals(Achievement.Tier.TWO, item.displayTier())
            assertEquals(47L, item.currentProgress)
            assertEquals(100L, (item as UiState.Earned).nextThreshold)
        }

        @Test
        fun `GIVEN no tier WHEN adapt THEN maps to Unearned`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = null,
                    currentProgress = 2,
                    nextThreshold = 5,
                )
            )

            val item = flatAchievements(result).single()
            assertInstanceOf(UiState.Unearned::class.java, item)
            assertFalse(item.isCompleted())
            assertEquals(null, item.currentTier)
            assertEquals(Achievement.Tier.ONE, item.displayTier())
            assertEquals(2L, item.currentProgress)
            assertEquals(5L, (item as UiState.Unearned).nextThreshold)
        }

        @Test
        fun `GIVEN no tier and zero progress WHEN adapt THEN maps to Unearned with no progress`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.BLIND_MODE_WINS,
                    currentTier = null,
                    currentProgress = 0,
                    nextThreshold = 1,
                )
            )

            val item = flatAchievements(result).single()
            assertInstanceOf(UiState.Unearned::class.java, item)
            assertFalse(item.hasProgress())
            assertEquals(0f, item.progress())
        }

        @Test
        fun `GIVEN unseen complete WHEN adapt THEN unseen is preserved`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.FIVE,
                    currentProgress = 1000,
                    nextThreshold = null,
                    isUnseen = true,
                )
            )

            assertTrue(flatAchievements(result).single().unseen)
        }

        @Test
        fun `GIVEN unseen earned WHEN adapt THEN unseen is preserved`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.TWO,
                    currentProgress = 50,
                    nextThreshold = 100,
                    isUnseen = true,
                )
            )

            assertTrue(flatAchievements(result).single().unseen)
        }

        @Test
        fun `GIVEN unearned WHEN adapt THEN unseen is always false`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = null,
                    currentProgress = 3,
                    nextThreshold = 5,
                    isUnseen = true,
                )
            )

            assertFalse(flatAchievements(result).single().unseen)
        }

        @Test
        fun `GIVEN empty list WHEN adapt THEN returns empty categories and not loading`() {
            val result = underTest.adapt(emptyList())

            assertTrue(result.categories.isEmpty())
            assertFalse(result.isLoading)
        }
    }

    @Nested
    internal inner class ProgressComputation {
        @Test
        fun `GIVEN earned with partial progress WHEN progress THEN returns fraction`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.ONE,
                    currentProgress = 15,
                    nextThreshold = 25,
                )
            )

            assertEquals(0.6f, flatAchievements(result).single().progress(), 0.001f)
        }

        @Test
        fun `GIVEN unearned with partial progress WHEN progress THEN returns fraction`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.BLIND_MODE_WINS,
                    currentTier = null,
                    currentProgress = 3,
                    nextThreshold = 5,
                )
            )

            assertEquals(0.6f, flatAchievements(result).single().progress(), 0.001f)
        }

        @Test
        fun `GIVEN progress exceeding threshold WHEN progress THEN clamps to 1`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.ONE,
                    currentProgress = 30,
                    nextThreshold = 25,
                )
            )

            assertEquals(1f, flatAchievements(result).single().progress())
        }

        @Test
        fun `GIVEN complete WHEN progress THEN returns 1`() {
            val result = adapt(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.FIVE,
                    currentProgress = 1000,
                    nextThreshold = null,
                )
            )

            assertEquals(1f, flatAchievements(result).single().progress())
        }
    }

    @Nested
    internal inner class Sorting {
        @Test
        fun `GIVEN mixed states in same category WHEN adapt THEN earned before in-progress before zero`() {
            val result = adapt(
                achievementState(Achievement.BLIND_MODE_WINS, null, 0, 1),
                achievementState(Achievement.BLIND_STRATEGIST, null, 3, 5),
                achievementState(Achievement.FIND_SQUARE_SESSIONS, Achievement.Tier.TWO, 50, 100),
            )

            val boardVision = findInCategory(result, AchievementCategory.BOARD_VISION)!!
            assertEquals(Achievement.FIND_SQUARE_SESSIONS, boardVision.achievements[0].achievement)
            assertEquals(Achievement.BLIND_STRATEGIST, boardVision.achievements[1].achievement)
            assertEquals(Achievement.BLIND_MODE_WINS, boardVision.achievements[2].achievement)
        }

        @Test
        fun `GIVEN complete and earned WHEN adapt THEN complete before earned`() {
            val result = adapt(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.TWO, 50, 100),
                achievementState(Achievement.RATING_CLIMBER, Achievement.Tier.FIVE, 2300, null),
            )

            val puzzles = findInCategory(result, AchievementCategory.PUZZLES)!!
            assertEquals(Achievement.RATING_CLIMBER, puzzles.achievements[0].achievement)
            assertEquals(Achievement.RATED_PUZZLES_SOLVED, puzzles.achievements[1].achievement)
        }

        @Test
        fun `GIVEN same tier WHEN adapt THEN higher progress fraction first`() {
            val result = adapt(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.ONE, 6, 25),
                achievementState(Achievement.FAILED_PUZZLES_REDEEMED, Achievement.Tier.ONE, 20, 25),
            )

            val puzzles = findInCategory(result, AchievementCategory.PUZZLES)!!
            assertEquals(Achievement.FAILED_PUZZLES_REDEEMED, puzzles.achievements[0].achievement)
            assertEquals(Achievement.RATED_PUZZLES_SOLVED, puzzles.achievements[1].achievement)
        }

        @Test
        fun `GIVEN higher tier but lower progress fraction WHEN adapt THEN higher tier first`() {
            val result = adapt(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.ONE, 24, 25),
                achievementState(Achievement.FAILED_PUZZLES_REDEEMED, Achievement.Tier.THREE, 101, 250),
            )

            val puzzles = findInCategory(result, AchievementCategory.PUZZLES)!!
            assertEquals(Achievement.FAILED_PUZZLES_REDEEMED, puzzles.achievements[0].achievement)
            assertEquals(Achievement.RATED_PUZZLES_SOLVED, puzzles.achievements[1].achievement)
        }

        @Test
        fun `GIVEN same bucket and progress WHEN adapt THEN falls back to enum declaration order`() {
            val result = adapt(
                achievementState(Achievement.BLIND_MODE_WINS, null, 0, 1),
                achievementState(Achievement.FIND_SQUARE_SESSIONS, null, 0, 5),
            )

            val boardVision = findInCategory(result, AchievementCategory.BOARD_VISION)!!
            assertEquals(Achievement.FIND_SQUARE_SESSIONS, boardVision.achievements[0].achievement)
            assertEquals(Achievement.BLIND_MODE_WINS, boardVision.achievements[1].achievement)
        }

        @Test
        fun `GIVEN multiple in-progress WHEN adapt THEN sorted by progress fraction descending`() {
            val result = adapt(
                achievementState(Achievement.FIND_SQUARE_SESSIONS, null, 1, 5),
                achievementState(Achievement.MOVE_PIECE_SESSIONS, null, 4, 5),
                achievementState(Achievement.BLIND_MODE_WINS, null, 2, 5),
            )

            val boardVision = findInCategory(result, AchievementCategory.BOARD_VISION)!!
            assertEquals(Achievement.MOVE_PIECE_SESSIONS, boardVision.achievements[0].achievement)
            assertEquals(Achievement.BLIND_MODE_WINS, boardVision.achievements[1].achievement)
            assertEquals(Achievement.FIND_SQUARE_SESSIONS, boardVision.achievements[2].achievement)
        }
    }

    @Nested
    internal inner class Grouping {
        @Test
        fun `GIVEN achievements from different categories WHEN adapt THEN grouped correctly`() {
            val result = adapt(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, null, 5, 25),
                achievementState(Achievement.PUZZLE_RUSH_SESSIONS, null, 3, 5),
                achievementState(Achievement.FIND_SQUARE_SESSIONS, null, 10, 25),
                achievementState(Achievement.TIME_INVESTED, null, 1, 10),
            )

            assertEquals(4, result.categories.size)
            assertEquals(AchievementCategory.PUZZLES, result.categories[0].category)
            assertEquals(AchievementCategory.RUSH_AND_STREAK, result.categories[1].category)
            assertEquals(AchievementCategory.BOARD_VISION, result.categories[2].category)
            assertEquals(AchievementCategory.DEDICATION, result.categories[3].category)
        }

        @Test
        fun `GIVEN empty category WHEN adapt THEN category is omitted`() {
            val result = adapt(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, null, 5, 25),
            )

            assertEquals(1, result.categories.size)
            assertEquals(AchievementCategory.PUZZLES, result.categories[0].category)
        }

        @Test
        fun `GIVEN category with mixed states WHEN adapt THEN earnedCount is correct`() {
            val result = adapt(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.FIVE, 1000, null),
                achievementState(Achievement.FAILED_PUZZLES_REDEEMED, Achievement.Tier.TWO, 50, 100),
                achievementState(Achievement.RATED_WIN_STREAK, null, 0, 3),
            )

            val puzzles = findInCategory(result, AchievementCategory.PUZZLES)!!
            assertEquals(2, puzzles.earnedCount)
            assertEquals(3, puzzles.totalCount)
        }

        @Test
        fun `GIVEN categories WHEN adapt THEN preserve enum ordering`() {
            val result = adapt(
                achievementState(Achievement.TIME_INVESTED, null, 1, 10),
                achievementState(Achievement.RATED_PUZZLES_SOLVED, null, 5, 25),
                achievementState(Achievement.FIND_SQUARE_SESSIONS, null, 10, 25),
            )

            assertEquals(AchievementCategory.PUZZLES, result.categories[0].category)
            assertEquals(AchievementCategory.BOARD_VISION, result.categories[1].category)
            assertEquals(AchievementCategory.DEDICATION, result.categories[2].category)
        }
    }

    @Nested
    internal inner class Summary {
        @Test
        fun `GIVEN mixed states WHEN adapt THEN summary counts are correct`() {
            val result = adapt(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.FIVE, 1000, null),
                achievementState(Achievement.FAILED_PUZZLES_REDEEMED, Achievement.Tier.TWO, 50, 100),
                achievementState(Achievement.RATED_WIN_STREAK, null, 2, 3),
                achievementState(Achievement.RATING_CLIMBER, null, 0, 1300),
            )

            val summary = result.summary
            assertEquals(1, summary.totalEarned)
            assertEquals(4, summary.totalAchievements)
            assertEquals(2, summary.inProgress)
            assertEquals(1, summary.locked)
        }

        @Test
        fun `GIVEN empty list WHEN adapt THEN summary is zeroed`() {
            val result = underTest.adapt(emptyList())

            assertEquals(0, result.summary.totalEarned)
            assertEquals(0, result.summary.totalAchievements)
            assertEquals(0, result.summary.inProgress)
            assertEquals(0, result.summary.locked)
        }

        @Test
        fun `GIVEN all complete WHEN adapt THEN all counted as earned`() {
            val result = adapt(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.FIVE, 1000, null),
                achievementState(Achievement.PUZZLE_RUSH_SESSIONS, Achievement.Tier.FIVE, 1000, null),
            )

            assertEquals(2, result.summary.totalEarned)
            assertEquals(0, result.summary.inProgress)
            assertEquals(0, result.summary.locked)
        }
    }

    @Nested
    internal inner class Validation {
        @Test
        fun `GIVEN zero nextThreshold for unearned WHEN adapt THEN throws`() {
            assertThrows<IllegalArgumentException> {
                adapt(achievementState(Achievement.RATED_PUZZLES_SOLVED, null, 0, 0))
            }
        }

        @Test
        fun `GIVEN zero nextThreshold for earned WHEN adapt THEN throws`() {
            assertThrows<IllegalArgumentException> {
                adapt(achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.ONE, 10, 0))
            }
        }
    }

    private fun achievementState(
        achievement: Achievement,
        currentTier: Achievement.Tier? = null,
        currentProgress: Long = 0,
        nextThreshold: Long? = null,
        isUnseen: Boolean = false,
    ): AchievementState = AchievementState(
        achievement = achievement,
        currentTier = currentTier,
        currentProgress = currentProgress,
        nextThreshold = nextThreshold,
        isUnseen = isUnseen,
    )
}
