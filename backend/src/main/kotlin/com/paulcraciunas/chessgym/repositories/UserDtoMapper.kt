package com.paulcraciunas.chessgym.repositories

import com.paulcraciunas.chessgym.models.*

@Suppress("UNCHECKED_CAST")
class UserDtoMapper {

    fun fromMap(data: Map<String, Any>): UserDto {
        val profileMap = data["profile"].asMap()
        val ratingsMap = data["ratings"].asMap()
        val highScoresMap = data["highScores"].asMap()
        val statisticsMap = data["statistics"].asMap()
        val achievementsMap = data["achievements"].asMap()

        return UserDto(
            deviceId = data["deviceId"] as? String ?: "",
            profile = parseProfile(profileMap),
            ratings = parseRatings(ratingsMap),
            highScores = parseHighScores(highScoresMap),
            statistics = parseStatistics(statisticsMap),
            achievements = parseAchievements(achievementsMap),
        )
    }

    fun asMap(user: UserDto): Map<String, Any?> = mapOf(
        "deviceId" to user.deviceId,
        "profile" to mapOf(
            "displayName" to user.profile.displayName,
            "joinDate" to user.profile.joinDate,
            "isSupporter" to user.profile.isSupporter,
            "lastModified" to user.profile.lastModified,
        ),
        "ratings" to mapOf(
            "current" to user.ratings.current,
            "blindMode" to user.ratings.blindMode,
        ),
        "highScores" to mapOf(
            "ratedPuzzle" to user.highScores.ratedPuzzle,
            "puzzleRush" to user.highScores.puzzleRush,
            "puzzleStreak" to user.highScores.puzzleStreak,
            "findTheSquare" to user.highScores.findTheSquare,
            "moveThePiece" to user.highScores.moveThePiece,
            "blindMode" to user.highScores.blindMode,
        ),
        "statistics" to mapOf(
            "puzzlesPlayed" to user.statistics.puzzlesPlayed,
            "puzzlesSolved" to user.statistics.puzzlesSolved,
            "totalTimeSpent" to user.statistics.totalTimeSpent,
            "ratedPuzzlesSolved" to user.statistics.ratedPuzzlesSolved,
            "puzzleRushSessions" to user.statistics.puzzleRushSessions,
            "streakSessions" to user.statistics.streakSessions,
            "failedPuzzlesRedeemed" to user.statistics.failedPuzzlesRedeemed,
            "findSquareSessions" to user.statistics.findSquareSessions,
            "moveThePieceSessions" to user.statistics.moveThePieceSessions,
            "blindModeWins" to user.statistics.blindModeWins,
            "rushPuzzlesSolved" to user.statistics.rushPuzzlesSolved,
        ),
        "achievements" to mapOf(
            "progress" to user.achievements.progress,
            "lastActiveDate" to user.achievements.lastActiveDate,
            "consecutiveDaysStreak" to user.achievements.consecutiveDaysStreak,
            "bestConsecutiveDaysStreak" to user.achievements.bestConsecutiveDaysStreak,
            "currentRatedWinStreak" to user.achievements.currentRatedWinStreak,
            "bestRatedWinStreak" to user.achievements.bestRatedWinStreak,
        ),
    )

    private fun parseProfile(data: Map<String, Any>): ProfileDto = ProfileDto(
        displayName = data["displayName"] as? String ?: "",
        joinDate = data["joinDate"] as? String,
        isSupporter = data["isSupporter"] as? Boolean ?: false,
        lastModified = data["lastModified"].asLong(),
    )

    private fun parseRatings(data: Map<String, Any>): RatingsDto = RatingsDto(
        current = data["current"].asInt(RatingsDto.DEFAULT_RATED_PUZZLE_RATING),
        blindMode = data["blindMode"].asInt(RatingsDto.DEFAULT_BLIND_MODE_RATING),
    )

    private fun parseHighScores(data: Map<String, Any>): HighScoresDto = HighScoresDto(
        ratedPuzzle = data["ratedPuzzle"].asInt(RatingsDto.DEFAULT_RATED_PUZZLE_RATING),
        puzzleRush = data["puzzleRush"].asInt(),
        puzzleStreak = data["puzzleStreak"].asInt(),
        findTheSquare = data["findTheSquare"].asInt(),
        moveThePiece = data["moveThePiece"].asInt(),
        blindMode = data["blindMode"].asInt(RatingsDto.DEFAULT_BLIND_MODE_RATING),
    )

    private fun parseStatistics(data: Map<String, Any>): StatisticsDto = StatisticsDto(
        puzzlesPlayed = data["puzzlesPlayed"].asInt(),
        puzzlesSolved = data["puzzlesSolved"].asInt(),
        totalTimeSpent = data["totalTimeSpent"].asLong(),
        ratedPuzzlesSolved = data["ratedPuzzlesSolved"].asInt(),
        puzzleRushSessions = data["puzzleRushSessions"].asInt(),
        streakSessions = data["streakSessions"].asInt(),
        failedPuzzlesRedeemed = data["failedPuzzlesRedeemed"].asInt(),
        findSquareSessions = data["findSquareSessions"].asInt(),
        moveThePieceSessions = data["moveThePieceSessions"].asInt(),
        blindModeWins = data["blindModeWins"].asInt(),
        rushPuzzlesSolved = data["rushPuzzlesSolved"].asInt(),
    )

    private fun parseAchievements(data: Map<String, Any>): AchievementsDto {
        val progressMap = data["progress"].asMap()
        return AchievementsDto(
            progress = progressMap.mapValues { (_, v) -> v.asLong() },
            lastActiveDate = data["lastActiveDate"] as? String,
            consecutiveDaysStreak = data["consecutiveDaysStreak"].asInt(),
            bestConsecutiveDaysStreak = data["bestConsecutiveDaysStreak"].asInt(),
            currentRatedWinStreak = data["currentRatedWinStreak"].asInt(),
            bestRatedWinStreak = data["bestRatedWinStreak"].asInt(),
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun Any?.asMap(): Map<String, Any> =
    this as? Map<String, Any> ?: emptyMap()

private fun Any?.asInt(defaultVal: Int = 0): Int = (this as? Number)?.toInt() ?: defaultVal
private fun Any?.asLong(defaultVal: Long = 0L): Long = (this as? Number)?.toLong() ?: defaultVal
