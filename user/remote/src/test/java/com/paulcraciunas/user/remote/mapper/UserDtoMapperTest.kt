package com.paulcraciunas.user.remote.mapper

import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.remote.model.AchievementsDto
import com.paulcraciunas.user.remote.model.ProfileDto
import com.paulcraciunas.user.remote.model.UserDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class UserDtoMapperTest {
    private val underTest = UserDtoMapper()

    @Test
    fun `GIVEN default user WHEN toDto THEN maps all fields correctly`() {
        // Given
        val user = User()

        // When
        val dto = underTest.toDto(user)

        // Then
        assertEquals("ChessEnthusiast", dto.profile.displayName)
        assertNotNull(dto.profile.joinDate)
        assert(dto.profile.lastModified > 0)
        assertEquals(1000, dto.ratings.current)
        assertEquals(400, dto.ratings.blindMode)
        assertEquals(1000, dto.highScores.ratedPuzzle)
        assertEquals(0, dto.statistics.puzzlesPlayed)
    }

    @Test
    fun `GIVEN default dto WHEN fromDto THEN maps all fields correctly`() {
        // Given
        val dto = UserDto()

        // When
        val user = underTest.fromDto(dto)

        // Then
        assertEquals("ChessEnthusiast", user.profile.displayName)
        assertEquals(1000, user.ratings.current)
        assertEquals(400, user.ratings.blindMode)
        assertEquals(0, user.highScores.puzzleRush)
        assertEquals(0, user.statistics.puzzlesPlayed)
        assertEquals("", user.deviceId)
        assertNull(user.authentication)
        assertEquals(emptyList<User.HistoryItem>(), user.history)
        assertEquals(emptyList<Int>(), user.failedPuzzles)
    }

    @Test
    fun `GIVEN user with data WHEN round-trip THEN preserves shared fields`() {
        // Given
        val user = User(
            profile = User.Profile(
                displayName = "JohnDoe",
                joinDate = LocalDate.of(2024, 6, 15),
            ),
            ratings = User.Ratings(current = 1500, blindMode = 600),
            highScores = User.HighScores(
                ratedPuzzle = 1500,
                puzzleRush = 42,
                puzzleStreak = 10,
                findTheSquare = 99,
                moveThePiece = 88,
                blindMode = 600,
            ),
            statistics = User.Statistics(
                puzzlesPlayed = 100,
                puzzlesSolved = 80,
                totalTimeSpent = 3_600_000L,
                ratedPuzzlesSolved = 50,
                puzzleRushSessions = 5,
                streakSessions = 3,
                failedPuzzlesRedeemed = 10,
                findSquareSessions = 7,
                moveThePieceSessions = 4,
                blindModeWins = 12,
                rushPuzzlesSolved = 20,
            ),
            achievements = User.Achievements(
                progress = mapOf("RATING_CLIMBER" to 1500L, "BLIND_STRATEGIST" to 600L),
                lastActiveDate = LocalDate.of(2024, 12, 25),
                consecutiveDaysStreak = 7,
                bestConsecutiveDaysStreak = 14,
                currentRatedWinStreak = 3,
                bestRatedWinStreak = 5,
            ),
        )

        // When
        val dto = underTest.toDto(user)
        val roundTripped = underTest.fromDto(dto)

        // Then
        assertEquals(user.profile, roundTripped.profile)
        assertEquals(user.ratings.current, roundTripped.ratings.current)
        assertEquals(user.ratings.blindMode, roundTripped.ratings.blindMode)
        assertEquals(user.highScores, roundTripped.highScores)
        assertEquals(user.statistics, roundTripped.statistics)
        assertEquals(user.achievements.progress, roundTripped.achievements.progress)
        assertEquals(user.achievements.lastActiveDate, roundTripped.achievements.lastActiveDate)
        assertEquals(user.achievements.consecutiveDaysStreak, roundTripped.achievements.consecutiveDaysStreak)
        assertEquals(user.achievements.bestConsecutiveDaysStreak, roundTripped.achievements.bestConsecutiveDaysStreak)
    }

    @Test
    fun `GIVEN user with client-only fields WHEN toDto THEN strips client fields`() {
        // Given
        val user = User(
            deviceId = "device-123",
            history = listOf(
                User.HistoryItem(
                    timestamp = LocalDate.now(),
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(1, 10, 5000L)
                )
            ),
            failedPuzzles = listOf(1, 2, 3),
            authentication = User.AuthenticationState(
                provider = User.AuthenticationState.AuthProvider.GOOGLE,
                userId = "uid_abc",
            ),
        )

        // When
        val dto = underTest.toDto(user)
        val fromDto = underTest.fromDto(dto)

        // Then: client-only fields revert to defaults
        assertEquals("", fromDto.deviceId)
        assertEquals(emptyList<User.HistoryItem>(), fromDto.history)
        assertEquals(emptyList<Int>(), fromDto.failedPuzzles)
        assertNull(fromDto.authentication)
    }

    @Test
    fun `GIVEN dto with null dates WHEN fromDto THEN uses defaults`() {
        // Given
        val dto = UserDto(
            profile = ProfileDto(joinDate = null),
            achievements = AchievementsDto(lastActiveDate = null),
        )

        // When
        val user = underTest.fromDto(dto)

        // Then
        assertEquals(LocalDate.now(), user.profile.joinDate)
        assertNull(user.achievements.lastActiveDate)
    }

    @Test
    fun `GIVEN dto with malformed date WHEN fromDto THEN falls back to default`() {
        // Given
        val dto = UserDto(
            profile = ProfileDto(joinDate = "not-a-date"),
        )

        // When
        val user = underTest.fromDto(dto)

        // Then
        assertEquals(LocalDate.now(), user.profile.joinDate)
    }

    @Test
    fun `GIVEN user WHEN toDto THEN sets lastModified`() {
        // Given
        val user = User()
        val beforeMs = System.currentTimeMillis()

        // When
        val dto = underTest.toDto(user)

        // Then
        val afterMs = System.currentTimeMillis()
        assert(dto.profile.lastModified in beforeMs..afterMs)
    }
}
