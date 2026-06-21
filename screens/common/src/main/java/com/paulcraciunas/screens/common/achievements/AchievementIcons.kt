package com.paulcraciunas.screens.common.achievements

import androidx.annotation.DrawableRes
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R

@get:DrawableRes
val Achievement.iconRes: Int
    get() = when (this) {
        Achievement.RATED_PUZZLES_SOLVED -> R.drawable.achievement_rated_puzzle_solver
        Achievement.PUZZLE_RUSH_SESSIONS -> R.drawable.achievement_puzzle_rush_runner
        Achievement.STREAK_SESSIONS -> R.drawable.achievement_streak_chaser
        Achievement.FAILED_PUZZLES_REDEEMED -> R.drawable.achievement_redemption_seeker
        Achievement.FIND_SQUARE_SESSIONS -> R.drawable.achievement_square_spotter_sessions
        Achievement.KNIGHT_PATH_SESSIONS -> R.drawable.achievement_piece_navigator_sessions
        Achievement.BLIND_MODE_WINS -> R.drawable.achievement_homer_s_legacy
        Achievement.RATED_WIN_STREAK -> R.drawable.achievement_rated_win_streak
        Achievement.RATING_CLIMBER -> R.drawable.achievement_rating_climber
        Achievement.BLIND_STRATEGIST -> R.drawable.achievement_blind_strategist
        Achievement.RUSH_CHAMPION -> R.drawable.achievement_rush_champion
        Achievement.STREAK_LEGEND -> R.drawable.achievement_streak_legend
        Achievement.EAGLE_EYE -> R.drawable.achievement_eagle_eye
        Achievement.KNIGHTS_PATH -> R.drawable.achievement_knight_s_path
        Achievement.PUZZLE_ADDICT -> R.drawable.achievement_puzzle_addict
        Achievement.TIME_INVESTED -> R.drawable.achievement_time_invested
        Achievement.DAILY_GRINDER -> R.drawable.achievement_daily_grinder
        Achievement.CONSISTENCY_KING -> R.drawable.achievement_consistency_king
        Achievement.BOARD_VISION -> R.drawable.achievement_board_vision
        Achievement.RUSH_SOLVER -> R.drawable.achievement_rush_solver
    }

@get:DrawableRes
val Achievement.Tier.outlineRes: Int
    get() = when (this) {
        Achievement.Tier.ONE -> R.drawable.achievement_outline_tier_1
        Achievement.Tier.TWO -> R.drawable.achievement_outline_tier_2
        Achievement.Tier.THREE -> R.drawable.achievement_outline_tier_3
        Achievement.Tier.FOUR -> R.drawable.achievement_outline_tier_4
        Achievement.Tier.FIVE -> R.drawable.achievement_outline_tier_5
    }
