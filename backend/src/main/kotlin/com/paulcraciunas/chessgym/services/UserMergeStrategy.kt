package com.paulcraciunas.chessgym.services

import com.paulcraciunas.chessgym.models.*

class UserMergeStrategy {

    fun merge(existing: UserDto, incoming: UserDto): UserDto = UserDto(
        deviceId = existing.deviceId,
        profile = mergeProfile(existing.profile, incoming.profile),
        ratings = mergeRatings(existing.ratings, incoming.ratings),
        highScores = mergeHighScores(existing.highScores, incoming.highScores),
        statistics = mergeStatistics(existing.statistics, incoming.statistics),
        achievements = mergeAchievements(existing.achievements, incoming.achievements),
    )

    private fun mergeProfile(
        existing: ProfileDto,
        incoming: ProfileDto,
    ): ProfileDto = if (incoming.lastModified >= existing.lastModified) incoming else existing

    private fun mergeRatings(
        existing: RatingsDto,
        incoming: RatingsDto,
    ): RatingsDto = RatingsDto(
        current = maxOf(existing.current, incoming.current),
        blindMode = maxOf(existing.blindMode, incoming.blindMode),
    )

    private fun mergeHighScores(
        existing: HighScoresDto,
        incoming: HighScoresDto,
    ): HighScoresDto = HighScoresDto(
        ratedPuzzle = maxOf(existing.ratedPuzzle, incoming.ratedPuzzle),
        puzzleRush = maxOf(existing.puzzleRush, incoming.puzzleRush),
        puzzleStreak = maxOf(existing.puzzleStreak, incoming.puzzleStreak),
        findTheSquare = maxOf(existing.findTheSquare, incoming.findTheSquare),
        knightPath = maxOf(existing.knightPath, incoming.knightPath),
        blindMode = maxOf(existing.blindMode, incoming.blindMode),
    )

    private fun mergeStatistics(
        existing: StatisticsDto,
        incoming: StatisticsDto,
    ): StatisticsDto = StatisticsDto(
        puzzlesPlayed = maxOf(existing.puzzlesPlayed, incoming.puzzlesPlayed),
        puzzlesSolved = maxOf(existing.puzzlesSolved, incoming.puzzlesSolved),
        totalTimeSpent = maxOf(existing.totalTimeSpent, incoming.totalTimeSpent),
        ratedPuzzlesSolved = maxOf(existing.ratedPuzzlesSolved, incoming.ratedPuzzlesSolved),
        puzzleRushSessions = maxOf(existing.puzzleRushSessions, incoming.puzzleRushSessions),
        streakSessions = maxOf(existing.streakSessions, incoming.streakSessions),
        failedPuzzlesRedeemed = maxOf(
            existing.failedPuzzlesRedeemed,
            incoming.failedPuzzlesRedeemed,
        ),
        findSquareSessions = maxOf(existing.findSquareSessions, incoming.findSquareSessions),
        knightPathSessions = maxOf(
            existing.knightPathSessions,
            incoming.knightPathSessions,
        ),
        blindModeWins = maxOf(existing.blindModeWins, incoming.blindModeWins),
        rushPuzzlesSolved = maxOf(existing.rushPuzzlesSolved, incoming.rushPuzzlesSolved),
    )

    private fun mergeAchievements(
        existing: AchievementsDto,
        incoming: AchievementsDto,
    ): AchievementsDto {
        val mergedProgress = (existing.progress.keys + incoming.progress.keys)
            .associateWith { key ->
                maxOf(
                    existing.progress[key] ?: 0L,
                    incoming.progress[key] ?: 0L,
                )
            }
        return AchievementsDto(
            progress = mergedProgress,
            lastActiveDate = listOfNotNull(
                existing.lastActiveDate,
                incoming.lastActiveDate,
            ).maxOrNull(),
            consecutiveDaysStreak = maxOf(
                existing.consecutiveDaysStreak,
                incoming.consecutiveDaysStreak,
            ),
            bestConsecutiveDaysStreak = maxOf(
                existing.bestConsecutiveDaysStreak,
                incoming.bestConsecutiveDaysStreak,
            ),
            currentRatedWinStreak = maxOf(
                existing.currentRatedWinStreak,
                incoming.currentRatedWinStreak,
            ),
            bestRatedWinStreak = maxOf(
                existing.bestRatedWinStreak,
                incoming.bestRatedWinStreak,
            ),
        )
    }
}
