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

    @Nested
    internal inner class TypeMapping {
        @Test
        fun `GIVEN max tier WHEN adapt THEN maps to Complete`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.FIVE,
                    currentProgress = 1000,
                    nextThreshold = null,
                )
            )

            // When
            val result = underTest.adapt(states)

            // Then
            val item = result.achievements.single()
            assertInstanceOf(UiState.Complete::class.java, item)
            assertTrue(item.isCompleted())
            assertEquals(Achievement.Tier.FIVE, item.currentTier)
            assertEquals(1f, item.progress())
            assertTrue(item.hasProgress())
        }

        @Test
        fun `GIVEN tier but not max WHEN adapt THEN maps to Earned`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.TWO,
                    currentProgress = 47,
                    nextThreshold = 100,
                )
            )

            // When
            val result = underTest.adapt(states)

            // Then
            val item = result.achievements.single()
            assertInstanceOf(UiState.Earned::class.java, item)
            assertFalse(item.isCompleted())
            assertEquals(Achievement.Tier.TWO, item.currentTier)
            assertEquals(Achievement.Tier.TWO, item.displayTier())
            assertEquals(47L, item.currentProgress)
            assertEquals(100L, (item as UiState.Earned).nextThreshold)
        }

        @Test
        fun `GIVEN no tier WHEN adapt THEN maps to Unearned`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = null,
                    currentProgress = 2,
                    nextThreshold = 5,
                )
            )

            // When
            val result = underTest.adapt(states)

            // Then
            val item = result.achievements.single()
            assertInstanceOf(UiState.Unearned::class.java, item)
            assertFalse(item.isCompleted())
            assertEquals(null, item.currentTier)
            assertEquals(Achievement.Tier.ONE, item.displayTier())
            assertEquals(2L, item.currentProgress)
            assertEquals(5L, (item as UiState.Unearned).nextThreshold)
        }

        @Test
        fun `GIVEN no tier and zero progress WHEN adapt THEN maps to Unearned with no progress`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.BLIND_MODE_WINS,
                    currentTier = null,
                    currentProgress = 0,
                    nextThreshold = 1,
                )
            )

            // When
            val result = underTest.adapt(states)

            // Then
            val item = result.achievements.single()
            assertInstanceOf(UiState.Unearned::class.java, item)
            assertFalse(item.hasProgress())
            assertEquals(0f, item.progress())
        }

        @Test
        fun `GIVEN unseen complete WHEN adapt THEN unseen is preserved`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.FIVE,
                    currentProgress = 1000,
                    nextThreshold = null,
                    isUnseen = true,
                )
            )

            // When
            val result = underTest.adapt(states)

            // Then
            assertTrue(result.achievements.single().unseen)
        }

        @Test
        fun `GIVEN unseen earned WHEN adapt THEN unseen is preserved`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.TWO,
                    currentProgress = 50,
                    nextThreshold = 100,
                    isUnseen = true,
                )
            )

            // When
            val result = underTest.adapt(states)

            // Then
            assertTrue(result.achievements.single().unseen)
        }

        @Test
        fun `GIVEN unearned WHEN adapt THEN unseen is always false`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = null,
                    currentProgress = 3,
                    nextThreshold = 5,
                    isUnseen = true,
                )
            )

            // When
            val result = underTest.adapt(states)

            // Then
            assertFalse(result.achievements.single().unseen)
        }

        @Test
        fun `GIVEN empty list WHEN adapt THEN returns empty achievements and not loading`() {
            // When
            val result = underTest.adapt(emptyList())

            // Then
            assertTrue(result.achievements.isEmpty())
            assertFalse(result.isLoading)
        }
    }

    @Nested
    internal inner class ProgressComputation {
        @Test
        fun `GIVEN earned with partial progress WHEN progress THEN returns fraction`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.ONE,
                    currentProgress = 15,
                    nextThreshold = 25,
                )
            )

            // When
            val item = underTest.adapt(states).achievements.single()

            // Then
            assertEquals(0.6f, item.progress(), 0.001f)
        }

        @Test
        fun `GIVEN unearned with partial progress WHEN progress THEN returns fraction`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.BLIND_MODE_WINS,
                    currentTier = null,
                    currentProgress = 3,
                    nextThreshold = 5,
                )
            )

            // When
            val item = underTest.adapt(states).achievements.single()

            // Then
            assertEquals(0.6f, item.progress(), 0.001f)
        }

        @Test
        fun `GIVEN progress exceeding threshold WHEN progress THEN clamps to 1`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.ONE,
                    currentProgress = 30,
                    nextThreshold = 25,
                )
            )

            // When
            val item = underTest.adapt(states).achievements.single()

            // Then
            assertEquals(1f, item.progress())
        }

        @Test
        fun `GIVEN complete WHEN progress THEN returns 1`() {
            // Given
            val states = listOf(
                achievementState(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.FIVE,
                    currentProgress = 1000,
                    nextThreshold = null,
                )
            )

            // When
            val item = underTest.adapt(states).achievements.single()

            // Then
            assertEquals(1f, item.progress())
        }
    }

    @Nested
    internal inner class Sorting {
        @Test
        fun `GIVEN mixed states WHEN adapt THEN earned before in-progress before zero`() {
            // Given
            val states = listOf(
                achievementState(Achievement.BLIND_MODE_WINS, null, 0, 1),
                achievementState(Achievement.PUZZLE_RUSH_SESSIONS, null, 3, 5),
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.TWO, 50, 100),
            )

            // When
            val result = underTest.adapt(states)

            // Then
            assertEquals(Achievement.RATED_PUZZLES_SOLVED, result.achievements[0].achievement)
            assertEquals(Achievement.PUZZLE_RUSH_SESSIONS, result.achievements[1].achievement)
            assertEquals(Achievement.BLIND_MODE_WINS, result.achievements[2].achievement)
        }

        @Test
        fun `GIVEN complete and earned WHEN adapt THEN complete before earned`() {
            // Given
            val states = listOf(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.TWO, 50, 100),
                achievementState(Achievement.RATING_CLIMBER, Achievement.Tier.FIVE, 2300, null),
            )

            // When
            val result = underTest.adapt(states)

            // Then
            assertEquals(Achievement.RATING_CLIMBER, result.achievements[0].achievement)
            assertEquals(Achievement.RATED_PUZZLES_SOLVED, result.achievements[1].achievement)
        }

        @Test
        fun `GIVEN same tier WHEN adapt THEN higher progress fraction first`() {
            // Given
            val states = listOf(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.ONE, 6, 25),
                achievementState(Achievement.PUZZLE_RUSH_SESSIONS, Achievement.Tier.ONE, 20, 25),
            )

            // When
            val result = underTest.adapt(states)

            // Then — PUZZLE_RUSH at 80% before RATED_PUZZLES at 24%
            assertEquals(Achievement.PUZZLE_RUSH_SESSIONS, result.achievements[0].achievement)
            assertEquals(Achievement.RATED_PUZZLES_SOLVED, result.achievements[1].achievement)
        }

        @Test
        fun `GIVEN higher tier but lower progress fraction WHEN adapt THEN higher tier first`() {
            // Given
            val states = listOf(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.ONE, 24, 25),
                achievementState(Achievement.PUZZLE_RUSH_SESSIONS, Achievement.Tier.THREE, 101, 250),
            )

            // When
            val result = underTest.adapt(states)

            // Then — Tier THREE before Tier ONE regardless of progress%
            assertEquals(Achievement.PUZZLE_RUSH_SESSIONS, result.achievements[0].achievement)
            assertEquals(Achievement.RATED_PUZZLES_SOLVED, result.achievements[1].achievement)
        }

        @Test
        fun `GIVEN same bucket and progress WHEN adapt THEN falls back to enum declaration order`() {
            // Given — both unearned with 0 progress
            val states = listOf(
                achievementState(Achievement.BLIND_MODE_WINS, null, 0, 1),
                achievementState(Achievement.RATED_PUZZLES_SOLVED, null, 0, 5),
            )

            // When
            val result = underTest.adapt(states)

            // Then — RATED_PUZZLES_SOLVED is declared before BLIND_MODE_WINS
            assertEquals(Achievement.RATED_PUZZLES_SOLVED, result.achievements[0].achievement)
            assertEquals(Achievement.BLIND_MODE_WINS, result.achievements[1].achievement)
        }

        @Test
        fun `GIVEN multiple in-progress WHEN adapt THEN sorted by progress fraction descending`() {
            // Given
            val states = listOf(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, null, 1, 5),
                achievementState(Achievement.PUZZLE_RUSH_SESSIONS, null, 4, 5),
                achievementState(Achievement.STREAK_SESSIONS, null, 2, 5),
            )

            // When
            val result = underTest.adapt(states)

            // Then — 80% > 40% > 20%
            assertEquals(Achievement.PUZZLE_RUSH_SESSIONS, result.achievements[0].achievement)
            assertEquals(Achievement.STREAK_SESSIONS, result.achievements[1].achievement)
            assertEquals(Achievement.RATED_PUZZLES_SOLVED, result.achievements[2].achievement)
        }
    }

    @Nested
    internal inner class Validation {
        @Test
        fun `GIVEN zero nextThreshold for unearned WHEN adapt THEN throws`() {
            // Given
            val states = listOf(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, null, 0, 0),
            )

            // When / Then
            assertThrows<IllegalArgumentException> { underTest.adapt(states) }
        }

        @Test
        fun `GIVEN zero nextThreshold for earned WHEN adapt THEN throws`() {
            // Given
            val states = listOf(
                achievementState(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.ONE, 10, 0),
            )

            // When / Then
            assertThrows<IllegalArgumentException> { underTest.adapt(states) }
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
