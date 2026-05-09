package com.paulcraciunas.user.remote.mapper

import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.remote.model.AchievementsDto
import com.paulcraciunas.user.remote.model.HighScoresDto
import com.paulcraciunas.user.remote.model.ProfileDto
import com.paulcraciunas.user.remote.model.RatingsDto
import com.paulcraciunas.user.remote.model.StatisticsDto
import com.paulcraciunas.user.remote.model.UserDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

class UserDtoMapper @Inject constructor() {

    fun toDto(user: User): UserDto = UserDto(
        profile = mapProfile(user.profile),
        ratings = mapRatings(user.ratings),
        highScores = mapHighScores(user.highScores),
        statistics = mapStatistics(user.statistics),
        achievements = mapAchievements(user.achievements),
    )

    fun fromDto(dto: UserDto): User = User(
        profile = parseProfile(dto.profile),
        ratings = parseRatings(dto.ratings),
        highScores = parseHighScores(dto.highScores),
        statistics = parseStatistics(dto.statistics),
        achievements = parseAchievements(dto.achievements),
    )

    private fun mapProfile(profile: User.Profile): ProfileDto = ProfileDto(
        firstName = profile.firstName,
        lastName = profile.lastName,
        joinDate = profile.joinDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
        avatarUrl = profile.avatarUrl,
        lastModified = System.currentTimeMillis(),
    )

    private fun parseProfile(dto: ProfileDto): User.Profile = User.Profile(
        firstName = dto.firstName,
        lastName = dto.lastName,
        joinDate = dto.joinDate.parseLocalDate() ?: LocalDate.now(),
        avatarUrl = dto.avatarUrl,
    )

    private fun mapRatings(ratings: User.Ratings): RatingsDto = RatingsDto(
        current = ratings.current,
        blindMode = ratings.blindMode,
    )

    private fun parseRatings(dto: RatingsDto): User.Ratings = User.Ratings(
        current = dto.current,
        blindMode = dto.blindMode,
    )

    private fun mapHighScores(highScores: User.HighScores): HighScoresDto = HighScoresDto(
        ratedPuzzle = highScores.ratedPuzzle,
        puzzleRush = highScores.puzzleRush,
        puzzleStreak = highScores.puzzleStreak,
        findTheSquare = highScores.findTheSquare,
        moveThePiece = highScores.moveThePiece,
        blindMode = highScores.blindMode,
    )

    private fun parseHighScores(dto: HighScoresDto): User.HighScores = User.HighScores(
        ratedPuzzle = dto.ratedPuzzle,
        puzzleRush = dto.puzzleRush,
        puzzleStreak = dto.puzzleStreak,
        findTheSquare = dto.findTheSquare,
        moveThePiece = dto.moveThePiece,
        blindMode = dto.blindMode,
    )

    private fun mapStatistics(stats: User.Statistics): StatisticsDto = StatisticsDto(
        puzzlesPlayed = stats.puzzlesPlayed,
        puzzlesSolved = stats.puzzlesSolved,
        totalTimeSpent = stats.totalTimeSpent,
        ratedPuzzlesSolved = stats.ratedPuzzlesSolved,
        puzzleRushSessions = stats.puzzleRushSessions,
        streakSessions = stats.streakSessions,
        failedPuzzlesRedeemed = stats.failedPuzzlesRedeemed,
        findSquareSessions = stats.findSquareSessions,
        moveThePieceSessions = stats.moveThePieceSessions,
        blindModeWins = stats.blindModeWins,
        rushPuzzlesSolved = stats.rushPuzzlesSolved,
    )

    private fun parseStatistics(dto: StatisticsDto): User.Statistics = User.Statistics(
        puzzlesPlayed = dto.puzzlesPlayed,
        puzzlesSolved = dto.puzzlesSolved,
        totalTimeSpent = dto.totalTimeSpent,
        ratedPuzzlesSolved = dto.ratedPuzzlesSolved,
        puzzleRushSessions = dto.puzzleRushSessions,
        streakSessions = dto.streakSessions,
        failedPuzzlesRedeemed = dto.failedPuzzlesRedeemed,
        findSquareSessions = dto.findSquareSessions,
        moveThePieceSessions = dto.moveThePieceSessions,
        blindModeWins = dto.blindModeWins,
        rushPuzzlesSolved = dto.rushPuzzlesSolved,
    )

    private fun mapAchievements(achievements: User.Achievements): AchievementsDto = AchievementsDto(
        progress = achievements.progress,
        lastActiveDate = achievements.lastActiveDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        consecutiveDaysStreak = achievements.consecutiveDaysStreak,
        bestConsecutiveDaysStreak = achievements.bestConsecutiveDaysStreak,
        currentRatedWinStreak = achievements.currentRatedWinStreak,
        bestRatedWinStreak = achievements.bestRatedWinStreak,
    )

    private fun parseAchievements(dto: AchievementsDto): User.Achievements = User.Achievements(
        progress = dto.progress,
        lastActiveDate = dto.lastActiveDate.parseLocalDate(),
        consecutiveDaysStreak = dto.consecutiveDaysStreak,
        bestConsecutiveDaysStreak = dto.bestConsecutiveDaysStreak,
        currentRatedWinStreak = dto.currentRatedWinStreak,
        bestRatedWinStreak = dto.bestRatedWinStreak,
    )
}

private fun String?.parseLocalDate(): LocalDate? = this?.let {
    try {
        LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: DateTimeParseException) {
        null
    }
}
