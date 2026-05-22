package com.paulcraciunas.chessgym.repositories

import com.paulcraciunas.chessgym.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserDtoMapperTest {
    private val mapper = UserDtoMapper()

    @Test
    fun `round-trip with default user preserves all fields`() {
        val original = UserDto()
        val map = mapper.asMap(original)
        val restored = mapper.fromMap(map.mapValues { it.value ?: Any() })

        assertEquals(original, restored)
    }

    @Test
    fun `round-trip with fully populated user preserves all fields`() {
        val original = UserDto(
            deviceId = "device-abc-123",
            profile = ProfileDto(
                displayName = "PaulCraciunas",
                joinDate = "2025-01-15",
                isSupporter = true,
                lastModified = 1717000000L,
            ),
            ratings = RatingsDto(current = 1500, blindMode = 800),
            highScores = HighScoresDto(
                ratedPuzzle = 1600, puzzleRush = 25, puzzleStreak = 40,
                findTheSquare = 90, moveThePiece = 85, blindMode = 900,
            ),
            statistics = StatisticsDto(
                puzzlesPlayed = 1000, puzzlesSolved = 800, totalTimeSpent = 360000L,
                ratedPuzzlesSolved = 500, puzzleRushSessions = 50, streakSessions = 30,
                failedPuzzlesRedeemed = 100, findSquareSessions = 75, moveThePieceSessions = 40,
                blindModeWins = 20, rushPuzzlesSolved = 350,
            ),
            achievements = AchievementsDto(
                progress = mapOf("RATING_CLIMBER" to 1500L, "PUZZLE_ADDICT" to 1000L),
                lastActiveDate = "2026-05-01",
                consecutiveDaysStreak = 7,
                bestConsecutiveDaysStreak = 14,
                currentRatedWinStreak = 5,
                bestRatedWinStreak = 12,
            ),
        )

        val map = mapper.asMap(original)
        @Suppress("UNCHECKED_CAST")
        val restored = mapper.fromMap(map as Map<String, Any>)

        assertEquals(original, restored)
    }

    @Test
    fun `fromMap handles empty map gracefully with defaults`() {
        val dto = mapper.fromMap(emptyMap())
        assertEquals(UserDto(), dto)
    }

    @Test
    fun `fromMap handles missing nested fields with defaults`() {
        val data = mapOf(
            "profile" to mapOf("displayName" to "Test"),
            "ratings" to emptyMap<String, Any>(),
        )

        val dto = mapper.fromMap(data)
        assertEquals("Test", dto.profile.displayName)
        assertNull(dto.profile.joinDate)
        assertEquals(RatingsDto.DEFAULT_RATED_PUZZLE_RATING, dto.ratings.current)
    }

    @Test
    fun `fromMap handles Number types correctly (Long stored as Double)`() {
        val data = mapOf(
            "statistics" to mapOf(
                "puzzlesPlayed" to 42.0,
                "totalTimeSpent" to 99999.0,
            ),
            "achievements" to mapOf(
                "progress" to mapOf("ACH_1" to 100.0),
                "consecutiveDaysStreak" to 5.0,
            ),
        )

        val dto = mapper.fromMap(data)
        assertEquals(42, dto.statistics.puzzlesPlayed)
        assertEquals(99999L, dto.statistics.totalTimeSpent)
        assertEquals(100L, dto.achievements.progress["ACH_1"])
        assertEquals(5, dto.achievements.consecutiveDaysStreak)
    }

    @Test
    fun `asMap includes null values for nullable fields`() {
        val user = UserDto(
            profile = ProfileDto(joinDate = null),
        )
        val map = mapper.asMap(user)
        @Suppress("UNCHECKED_CAST")
        val profileMap = map["profile"] as Map<String, Any?>
        assertNull(profileMap["joinDate"])
    }

    @Test
    fun `fromMap handles wrongly typed values by falling back to defaults`() {
        val data = mapOf(
            "profile" to mapOf("displayName" to 12345),
            "ratings" to mapOf("current" to "not-a-number"),
        )

        val dto = mapper.fromMap(data)
        assertEquals("", dto.profile.displayName)
        assertEquals(RatingsDto.DEFAULT_RATED_PUZZLE_RATING, dto.ratings.current)
    }

    @Test
    fun `fromMap handles empty achievements progress map`() {
        val data = mapOf(
            "achievements" to mapOf(
                "progress" to emptyMap<String, Any>(),
            ),
        )

        val dto = mapper.fromMap(data)
        assertEquals(emptyMap(), dto.achievements.progress)
    }
}
