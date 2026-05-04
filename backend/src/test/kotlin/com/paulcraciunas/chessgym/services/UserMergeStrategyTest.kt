package com.paulcraciunas.chessgym.services

import com.paulcraciunas.chessgym.models.*
import kotlin.test.Test
import kotlin.test.assertEquals

class UserMergeStrategyTest {
    private val strategy = UserMergeStrategy()

    @Test
    fun `merge takes higher numeric values`() {
        val existing = UserDto(
            ratings = RatingsDto(current = 1200, blindMode = 500),
            highScores = HighScoresDto(ratedPuzzle = 1250, puzzleRush = 10),
            statistics = StatisticsDto(puzzlesPlayed = 100, puzzlesSolved = 80),
        )
        val incoming = UserDto(
            ratings = RatingsDto(current = 1100, blindMode = 600),
            highScores = HighScoresDto(ratedPuzzle = 1200, puzzleRush = 15),
            statistics = StatisticsDto(puzzlesPlayed = 90, puzzlesSolved = 85),
        )

        val merged = strategy.merge(existing, incoming)

        assertEquals(1200, merged.ratings.current)
        assertEquals(600, merged.ratings.blindMode)
        assertEquals(1250, merged.highScores.ratedPuzzle)
        assertEquals(15, merged.highScores.puzzleRush)
        assertEquals(100, merged.statistics.puzzlesPlayed)
        assertEquals(85, merged.statistics.puzzlesSolved)
    }

    @Test
    fun `merge uses last-write-wins for profile based on timestamp`() {
        val existing = UserDto(
            profile = ProfileDto(
                firstName = "OldName",
                lastName = "OldLast",
                lastModified = 1000L,
            ),
        )
        val incoming = UserDto(
            profile = ProfileDto(
                firstName = "NewName",
                lastName = "NewLast",
                lastModified = 2000L,
            ),
        )

        val merged = strategy.merge(existing, incoming)
        assertEquals("NewName", merged.profile.firstName)
        assertEquals("NewLast", merged.profile.lastName)
    }

    @Test
    fun `merge keeps existing profile when incoming timestamp is older`() {
        val existing = UserDto(
            profile = ProfileDto(
                firstName = "KeepMe",
                lastModified = 3000L,
            ),
        )
        val incoming = UserDto(
            profile = ProfileDto(
                firstName = "DiscardMe",
                lastModified = 1000L,
            ),
        )

        val merged = strategy.merge(existing, incoming)
        assertEquals("KeepMe", merged.profile.firstName)
    }

    @Test
    fun `merge uses incoming profile when timestamps are equal`() {
        val existing = UserDto(
            profile = ProfileDto(firstName = "Existing", lastModified = 1000L),
        )
        val incoming = UserDto(
            profile = ProfileDto(firstName = "Incoming", lastModified = 1000L),
        )

        val merged = strategy.merge(existing, incoming)
        assertEquals("Incoming", merged.profile.firstName)
    }

    @Test
    fun `merge combines achievement progress taking max per key`() {
        val existing = UserDto(
            achievements = AchievementsDto(
                progress = mapOf("RATING_CLIMBER" to 1200L, "PUZZLE_ADDICT" to 500L),
                bestConsecutiveDaysStreak = 10,
            ),
        )
        val incoming = UserDto(
            achievements = AchievementsDto(
                progress = mapOf("RATING_CLIMBER" to 1100L, "BLIND_STRATEGIST" to 600L),
                bestConsecutiveDaysStreak = 15,
            ),
        )

        val merged = strategy.merge(existing, incoming)
        assertEquals(1200L, merged.achievements.progress["RATING_CLIMBER"])
        assertEquals(500L, merged.achievements.progress["PUZZLE_ADDICT"])
        assertEquals(600L, merged.achievements.progress["BLIND_STRATEGIST"])
        assertEquals(15, merged.achievements.bestConsecutiveDaysStreak)
    }

    @Test
    fun `merge takes later lastActiveDate`() {
        val existing = UserDto(
            achievements = AchievementsDto(lastActiveDate = "2026-01-01"),
        )
        val incoming = UserDto(
            achievements = AchievementsDto(lastActiveDate = "2026-05-01"),
        )

        val merged = strategy.merge(existing, incoming)
        assertEquals("2026-05-01", merged.achievements.lastActiveDate)
    }

    @Test
    fun `merge handles null lastActiveDate on one side`() {
        val existing = UserDto(
            achievements = AchievementsDto(lastActiveDate = "2026-01-01"),
        )
        val incoming = UserDto(
            achievements = AchievementsDto(lastActiveDate = null),
        )

        val merged = strategy.merge(existing, incoming)
        assertEquals("2026-01-01", merged.achievements.lastActiveDate)
    }

    @Test
    fun `merge handles both null lastActiveDates`() {
        val existing = UserDto(achievements = AchievementsDto(lastActiveDate = null))
        val incoming = UserDto(achievements = AchievementsDto(lastActiveDate = null))

        val merged = strategy.merge(existing, incoming)
        assertEquals(null, merged.achievements.lastActiveDate)
    }

    @Test
    fun `merge handles empty progress maps`() {
        val existing = UserDto(achievements = AchievementsDto(progress = emptyMap()))
        val incoming = UserDto(
            achievements = AchievementsDto(progress = mapOf("ACH_1" to 42L)),
        )

        val merged = strategy.merge(existing, incoming)
        assertEquals(1, merged.achievements.progress.size)
        assertEquals(42L, merged.achievements.progress["ACH_1"])
    }

    @Test
    fun `merge preserves all highScore fields taking max`() {
        val existing = UserDto(
            highScores = HighScoresDto(
                ratedPuzzle = 1100, puzzleRush = 20, puzzleStreak = 30,
                findTheSquare = 40, moveThePiece = 50, blindMode = 500,
            ),
        )
        val incoming = UserDto(
            highScores = HighScoresDto(
                ratedPuzzle = 1200, puzzleRush = 10, puzzleStreak = 35,
                findTheSquare = 35, moveThePiece = 55, blindMode = 450,
            ),
        )

        val merged = strategy.merge(existing, incoming)
        assertEquals(1200, merged.highScores.ratedPuzzle)
        assertEquals(20, merged.highScores.puzzleRush)
        assertEquals(35, merged.highScores.puzzleStreak)
        assertEquals(40, merged.highScores.findTheSquare)
        assertEquals(55, merged.highScores.moveThePiece)
        assertEquals(500, merged.highScores.blindMode)
    }

    @Test
    fun `merge preserves all statistics fields taking max`() {
        val existing = UserDto(
            statistics = StatisticsDto(
                puzzlesPlayed = 100, puzzlesSolved = 80, totalTimeSpent = 5000L,
                ratedPuzzlesSolved = 50, puzzleRushSessions = 10, streakSessions = 5,
                failedPuzzlesRedeemed = 20, findSquareSessions = 15, moveThePieceSessions = 8,
                blindModeWins = 3, rushPuzzlesSolved = 60,
            ),
        )
        val incoming = UserDto(
            statistics = StatisticsDto(
                puzzlesPlayed = 110, puzzlesSolved = 75, totalTimeSpent = 4500L,
                ratedPuzzlesSolved = 55, puzzleRushSessions = 8, streakSessions = 7,
                failedPuzzlesRedeemed = 18, findSquareSessions = 20, moveThePieceSessions = 6,
                blindModeWins = 5, rushPuzzlesSolved = 55,
            ),
        )

        val merged = strategy.merge(existing, incoming)
        assertEquals(110, merged.statistics.puzzlesPlayed)
        assertEquals(80, merged.statistics.puzzlesSolved)
        assertEquals(5000L, merged.statistics.totalTimeSpent)
        assertEquals(55, merged.statistics.ratedPuzzlesSolved)
        assertEquals(10, merged.statistics.puzzleRushSessions)
        assertEquals(7, merged.statistics.streakSessions)
        assertEquals(20, merged.statistics.failedPuzzlesRedeemed)
        assertEquals(20, merged.statistics.findSquareSessions)
        assertEquals(8, merged.statistics.moveThePieceSessions)
        assertEquals(5, merged.statistics.blindModeWins)
        assertEquals(60, merged.statistics.rushPuzzlesSolved)
    }

    @Test
    fun `merge with two default users produces default user`() {
        val merged = strategy.merge(UserDto(), UserDto())
        assertEquals(UserDto(), merged)
    }
}
