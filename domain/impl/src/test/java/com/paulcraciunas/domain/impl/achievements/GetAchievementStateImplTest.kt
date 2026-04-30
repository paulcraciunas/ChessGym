package com.paulcraciunas.domain.impl.achievements

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.AchievementState
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GetAchievementStateImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val underTest = GetAchievementStateImpl(fakeUserRepository)

    @Test
    fun `GIVEN default user WHEN invoke THEN returns all 20 achievements with no tier`() = runTest {
        fakeUserRepository.update(User())

        val result = underTest()

        assertEquals(Achievement.entries.size, result.size)
        result.forEach { state ->
            assertNull(state.currentTier)
            assertEquals(0L, state.currentProgress)
            assertFalse(state.isUnseen)
            assertEquals(state.achievement.nextTierProgress(null), state.nextThreshold)
        }
    }

    @Test
    fun `GIVEN progress exactly at threshold WHEN invoke THEN returns that tier`() = runTest {
        val achievement = Achievement.RATED_PUZZLES_SOLVED
        val tierTwoThreshold = 25L
        fakeUserRepository.update(createUserWithProgress(achievement, tierTwoThreshold))

        val result = underTest().findAchievement(achievement)

        assertEquals(Achievement.Tier.TWO, result.currentTier)
        assertEquals(achievement.nextTierProgress(Achievement.Tier.TWO), result.nextThreshold)
    }

    @Test
    fun `GIVEN progress just below threshold WHEN invoke THEN returns previous tier`() = runTest {
        val achievement = Achievement.RATED_PUZZLES_SOLVED
        // 24 is just below Tier TWO (25)
        fakeUserRepository.update(createUserWithProgress(achievement, 24L))

        val result = underTest().findAchievement(achievement)

        assertEquals(Achievement.Tier.ONE, result.currentTier)
        assertEquals(25L, result.nextThreshold)
    }

    @Test
    fun `GIVEN all tiers completed WHEN invoke THEN returns max tier and null nextThreshold`() = runTest {
        val achievement = Achievement.RATED_PUZZLES_SOLVED
        fakeUserRepository.update(createUserWithProgress(achievement, 1000L))

        val result = underTest().findAchievement(achievement)

        assertEquals(Achievement.Tier.FIVE, result.currentTier)
        assertNull(result.nextThreshold)
    }

    @Test
    fun `GIVEN progress at tier 2 threshold WHEN invoke THEN returns tier TWO`() = runTest {
        val achievement = Achievement.RATED_PUZZLES_SOLVED
        fakeUserRepository.update(createUserWithProgress(achievement, 25L))

        val result = underTest().findAchievement(achievement)

        assertEquals(Achievement.Tier.TWO, result.currentTier)
        assertEquals(25L, result.currentProgress)
        assertEquals(100L, result.nextThreshold)
    }

    @Test
    fun `GIVEN all 5 tiers completed WHEN invoke THEN returns tier FIVE with null nextThreshold`() = runTest {
        val achievement = Achievement.RATED_PUZZLES_SOLVED
        fakeUserRepository.update(createUserWithProgress(achievement, 1000L))

        val result = underTest().findAchievement(achievement)

        assertEquals(Achievement.Tier.FIVE, result.currentTier)
        assertNull(result.nextThreshold)
    }

    @Test
    fun `GIVEN unseen achievements WHEN invoke THEN marks them as unseen`() = runTest {
        val achievement = Achievement.RATED_PUZZLES_SOLVED
        fakeUserRepository.update(createUserWithProgress(achievement, 25L, isUnseen = true))

        val result = underTest()

        val ratedPuzzles = result.first { it.achievement == Achievement.RATED_PUZZLES_SOLVED }
        assertTrue(ratedPuzzles.isUnseen)

        val rushSessions = result.first { it.achievement == Achievement.PUZZLE_RUSH_SESSIONS }
        assertFalse(rushSessions.isUnseen)
    }

    private fun List<AchievementState>.findAchievement(achievement: Achievement) =
        first { it.achievement == achievement }

    private fun createUserWithProgress(achievement: Achievement, progress: Long, isUnseen: Boolean = false) =
        User(
            achievements = User.Achievements(
                progress = mapOf(achievement.name to progress),
                unseenAchievements = if (isUnseen) setOf(achievement.name) else emptySet()
            )
        )
}
