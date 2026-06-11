package com.paulcraciunas.screens.common.achievements

import androidx.annotation.DrawableRes
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R

@get:DrawableRes
val Achievement.iconRes: Int
    get() = when (this) {
        Achievement.RATED_PUZZLES_SOLVED -> R.drawable.achievement_icon_01
        Achievement.PUZZLE_RUSH_SESSIONS -> R.drawable.achievement_icon_02
        Achievement.STREAK_SESSIONS -> R.drawable.achievement_icon_03
        Achievement.FAILED_PUZZLES_REDEEMED -> R.drawable.achievement_icon_04
        Achievement.FIND_SQUARE_SESSIONS -> R.drawable.achievement_icon_05
        Achievement.KNIGHT_PATH_SESSIONS -> R.drawable.achievement_icon_06
        Achievement.BLIND_MODE_WINS -> R.drawable.achievement_icon_07
        Achievement.RATED_WIN_STREAK -> R.drawable.achievement_icon_08
        Achievement.RATING_CLIMBER -> R.drawable.achievement_icon_09
        Achievement.BLIND_STRATEGIST -> R.drawable.achievement_icon_10
        Achievement.RUSH_CHAMPION -> R.drawable.achievement_icon_11
        Achievement.STREAK_LEGEND -> R.drawable.achievement_icon_12
        Achievement.EAGLE_EYE -> R.drawable.achievement_icon_13
        Achievement.KNIGHTS_PATH -> R.drawable.achievement_icon_14
        Achievement.PUZZLE_ADDICT -> R.drawable.achievement_icon_15
        Achievement.TIME_INVESTED -> R.drawable.military_medal_icon
        Achievement.DAILY_GRINDER -> R.drawable.achievement_icon_17
        Achievement.CONSISTENCY_KING -> R.drawable.military_medal_icon
        Achievement.BOARD_VISION -> R.drawable.achievement_icon_19
        Achievement.RUSH_SOLVER -> R.drawable.achievement_icon_20
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
