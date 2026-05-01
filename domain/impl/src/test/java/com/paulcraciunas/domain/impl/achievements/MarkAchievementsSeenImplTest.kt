package com.paulcraciunas.domain.impl.achievements

import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MarkAchievementsSeenImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val underTest = MarkAchievementsSeenImpl(fakeUserRepository)

    @Test
    fun `GIVEN unseen achievements WHEN invoke THEN clears unseen set`() = runTest {
        // Given
        val user = User(
            achievements = User.Achievements(
                unseenAchievements = setOf("RATED_PUZZLES_SOLVED"),
            ),
        )
        fakeUserRepository.update(user)

        // When
        underTest()

        // Then
        val updatedUser = fakeUserRepository.get()
        assertTrue(updatedUser.achievements.unseenAchievements.isEmpty())
    }

    @Test
    fun `GIVEN no unseen achievements WHEN invoke THEN does not update user`() = runTest {
        // Given
        val user = User(
            achievements = User.Achievements(unseenAchievements = emptySet()),
        )
        fakeUserRepository.update(user)

        // When
        underTest()

        // Then
        val updatedUser = fakeUserRepository.get()
        assertEquals(user, updatedUser)
    }
}
