package com.paulcraciunas.screens.achievements.vm

import com.paulcraciunas.domain.api.achievements.Achievement

enum class AchievementCategory {
    PUZZLES,
    RUSH_AND_STREAK,
    BOARD_VISION,
    SKILL_AND_MASTERY,
    DEDICATION,
}

val Achievement.category: AchievementCategory
    get() = when (this) {
        Achievement.RATED_PUZZLES_SOLVED,
        Achievement.FAILED_PUZZLES_REDEEMED,
        Achievement.RATED_WIN_STREAK,
        Achievement.RATING_CLIMBER,
        -> AchievementCategory.PUZZLES

        Achievement.PUZZLE_RUSH_SESSIONS,
        Achievement.STREAK_SESSIONS,
        Achievement.RUSH_CHAMPION,
        Achievement.STREAK_LEGEND,
        -> AchievementCategory.RUSH_AND_STREAK

        Achievement.FIND_SQUARE_SESSIONS,
        Achievement.KNIGHT_PATH_SESSIONS,
        Achievement.BLIND_MODE_WINS,
        Achievement.BLIND_STRATEGIST,
        Achievement.BOARD_VISION,
        -> AchievementCategory.BOARD_VISION

        Achievement.EAGLE_EYE,
        Achievement.KNIGHTS_PATH,
        Achievement.PUZZLE_ADDICT,
        Achievement.RUSH_SOLVER,
        -> AchievementCategory.SKILL_AND_MASTERY

        Achievement.TIME_INVESTED,
        Achievement.DAILY_GRINDER,
        Achievement.CONSISTENCY_KING,
        -> AchievementCategory.DEDICATION
    }
