package com.paulcraciunas.screens.common.achievements

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R

@Composable
fun Achievement.displayName(): String = stringResource(displayNameRes)

@Composable
fun Achievement.description(): String = stringResource(descriptionRes)

@Composable
fun Achievement.tierName(tier: Achievement.Tier): String =
    stringResource(tierNameRes(this, tier))

@get:StringRes
private val Achievement.displayNameRes: Int
    get() = when (this) {
        Achievement.RATED_PUZZLES_SOLVED -> R.string.achievement_category_rated_puzzles_solved
        Achievement.PUZZLE_RUSH_SESSIONS -> R.string.achievement_category_puzzle_rush_sessions
        Achievement.STREAK_SESSIONS -> R.string.achievement_category_streak_sessions
        Achievement.FAILED_PUZZLES_REDEEMED -> R.string.achievement_category_failed_puzzles_redeemed
        Achievement.FIND_SQUARE_SESSIONS -> R.string.achievement_category_find_square_sessions
        Achievement.KNIGHT_PATH_SESSIONS -> R.string.achievement_category_knight_path_sessions
        Achievement.BLIND_MODE_WINS -> R.string.achievement_category_blind_mode_wins
        Achievement.RATED_WIN_STREAK -> R.string.achievement_category_rated_win_streak
        Achievement.RATING_CLIMBER -> R.string.achievement_category_rating_climber
        Achievement.BLIND_STRATEGIST -> R.string.achievement_category_blind_strategist
        Achievement.RUSH_CHAMPION -> R.string.achievement_category_rush_champion
        Achievement.STREAK_LEGEND -> R.string.achievement_category_streak_legend
        Achievement.EAGLE_EYE -> R.string.achievement_category_eagle_eye
        Achievement.KNIGHTS_PATH -> R.string.achievement_category_knights_path
        Achievement.PUZZLE_ADDICT -> R.string.achievement_category_puzzle_addict
        Achievement.TIME_INVESTED -> R.string.achievement_category_time_invested
        Achievement.DAILY_GRINDER -> R.string.achievement_category_daily_grinder
        Achievement.CONSISTENCY_KING -> R.string.achievement_category_consistency_king
        Achievement.BOARD_VISION -> R.string.achievement_category_board_vision
        Achievement.RUSH_SOLVER -> R.string.achievement_category_rush_solver
    }

@get:StringRes
private val Achievement.descriptionRes: Int
    get() = when (this) {
        Achievement.RATED_PUZZLES_SOLVED -> R.string.achievement_desc_rated_puzzles_solved
        Achievement.PUZZLE_RUSH_SESSIONS -> R.string.achievement_desc_puzzle_rush_sessions
        Achievement.STREAK_SESSIONS -> R.string.achievement_desc_streak_sessions
        Achievement.FAILED_PUZZLES_REDEEMED -> R.string.achievement_desc_failed_puzzles_redeemed
        Achievement.FIND_SQUARE_SESSIONS -> R.string.achievement_desc_find_square_sessions
        Achievement.KNIGHT_PATH_SESSIONS -> R.string.achievement_desc_knight_path_sessions
        Achievement.BLIND_MODE_WINS -> R.string.achievement_desc_blind_mode_wins
        Achievement.RATED_WIN_STREAK -> R.string.achievement_desc_rated_win_streak
        Achievement.RATING_CLIMBER -> R.string.achievement_desc_rating_climber
        Achievement.BLIND_STRATEGIST -> R.string.achievement_desc_blind_strategist
        Achievement.RUSH_CHAMPION -> R.string.achievement_desc_rush_champion
        Achievement.STREAK_LEGEND -> R.string.achievement_desc_streak_legend
        Achievement.EAGLE_EYE -> R.string.achievement_desc_eagle_eye
        Achievement.KNIGHTS_PATH -> R.string.achievement_desc_knights_path
        Achievement.PUZZLE_ADDICT -> R.string.achievement_desc_puzzle_addict
        Achievement.TIME_INVESTED -> R.string.achievement_desc_time_invested
        Achievement.DAILY_GRINDER -> R.string.achievement_desc_daily_grinder
        Achievement.CONSISTENCY_KING -> R.string.achievement_desc_consistency_king
        Achievement.BOARD_VISION -> R.string.achievement_desc_board_vision
        Achievement.RUSH_SOLVER -> R.string.achievement_desc_rush_solver
    }

@StringRes
private fun tierNameRes(achievement: Achievement, tier: Achievement.Tier): Int = when (achievement) {
    Achievement.RATED_PUZZLES_SOLVED -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_rated_puzzles_solved_one
        Achievement.Tier.TWO -> R.string.achievement_rated_puzzles_solved_two
        Achievement.Tier.THREE -> R.string.achievement_rated_puzzles_solved_three
        Achievement.Tier.FOUR -> R.string.achievement_rated_puzzles_solved_four
        Achievement.Tier.FIVE -> R.string.achievement_rated_puzzles_solved_five
    }
    Achievement.PUZZLE_RUSH_SESSIONS -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_puzzle_rush_sessions_one
        Achievement.Tier.TWO -> R.string.achievement_puzzle_rush_sessions_two
        Achievement.Tier.THREE -> R.string.achievement_puzzle_rush_sessions_three
        Achievement.Tier.FOUR -> R.string.achievement_puzzle_rush_sessions_four
        Achievement.Tier.FIVE -> R.string.achievement_puzzle_rush_sessions_five
    }
    Achievement.STREAK_SESSIONS -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_streak_sessions_one
        Achievement.Tier.TWO -> R.string.achievement_streak_sessions_two
        Achievement.Tier.THREE -> R.string.achievement_streak_sessions_three
        Achievement.Tier.FOUR -> R.string.achievement_streak_sessions_four
        Achievement.Tier.FIVE -> R.string.achievement_streak_sessions_five
    }
    Achievement.FAILED_PUZZLES_REDEEMED -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_failed_puzzles_redeemed_one
        Achievement.Tier.TWO -> R.string.achievement_failed_puzzles_redeemed_two
        Achievement.Tier.THREE -> R.string.achievement_failed_puzzles_redeemed_three
        Achievement.Tier.FOUR -> R.string.achievement_failed_puzzles_redeemed_four
        Achievement.Tier.FIVE -> R.string.achievement_failed_puzzles_redeemed_five
    }
    Achievement.FIND_SQUARE_SESSIONS -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_find_square_sessions_one
        Achievement.Tier.TWO -> R.string.achievement_find_square_sessions_two
        Achievement.Tier.THREE -> R.string.achievement_find_square_sessions_three
        Achievement.Tier.FOUR -> R.string.achievement_find_square_sessions_four
        Achievement.Tier.FIVE -> R.string.achievement_find_square_sessions_five
    }
    Achievement.KNIGHT_PATH_SESSIONS -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_knight_path_sessions_one
        Achievement.Tier.TWO -> R.string.achievement_knight_path_sessions_two
        Achievement.Tier.THREE -> R.string.achievement_knight_path_sessions_three
        Achievement.Tier.FOUR -> R.string.achievement_knight_path_sessions_four
        Achievement.Tier.FIVE -> R.string.achievement_knight_path_sessions_five
    }
    Achievement.BLIND_MODE_WINS -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_blind_mode_wins_one
        Achievement.Tier.TWO -> R.string.achievement_blind_mode_wins_two
        Achievement.Tier.THREE -> R.string.achievement_blind_mode_wins_three
        Achievement.Tier.FOUR -> R.string.achievement_blind_mode_wins_four
        Achievement.Tier.FIVE -> R.string.achievement_blind_mode_wins_five
    }
    Achievement.RATED_WIN_STREAK -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_rated_win_streak_one
        Achievement.Tier.TWO -> R.string.achievement_rated_win_streak_two
        Achievement.Tier.THREE -> R.string.achievement_rated_win_streak_three
        Achievement.Tier.FOUR -> R.string.achievement_rated_win_streak_four
        Achievement.Tier.FIVE -> R.string.achievement_rated_win_streak_five
    }
    Achievement.RATING_CLIMBER -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_rating_climber_one
        Achievement.Tier.TWO -> R.string.achievement_rating_climber_two
        Achievement.Tier.THREE -> R.string.achievement_rating_climber_three
        Achievement.Tier.FOUR -> R.string.achievement_rating_climber_four
        Achievement.Tier.FIVE -> R.string.achievement_rating_climber_five
    }
    Achievement.BLIND_STRATEGIST -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_blind_strategist_one
        Achievement.Tier.TWO -> R.string.achievement_blind_strategist_two
        Achievement.Tier.THREE -> R.string.achievement_blind_strategist_three
        Achievement.Tier.FOUR -> R.string.achievement_blind_strategist_four
        Achievement.Tier.FIVE -> R.string.achievement_blind_strategist_five
    }
    Achievement.RUSH_CHAMPION -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_rush_champion_one
        Achievement.Tier.TWO -> R.string.achievement_rush_champion_two
        Achievement.Tier.THREE -> R.string.achievement_rush_champion_three
        Achievement.Tier.FOUR -> R.string.achievement_rush_champion_four
        Achievement.Tier.FIVE -> R.string.achievement_rush_champion_five
    }
    Achievement.STREAK_LEGEND -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_streak_legend_one
        Achievement.Tier.TWO -> R.string.achievement_streak_legend_two
        Achievement.Tier.THREE -> R.string.achievement_streak_legend_three
        Achievement.Tier.FOUR -> R.string.achievement_streak_legend_four
        Achievement.Tier.FIVE -> R.string.achievement_streak_legend_five
    }
    Achievement.EAGLE_EYE -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_eagle_eye_one
        Achievement.Tier.TWO -> R.string.achievement_eagle_eye_two
        Achievement.Tier.THREE -> R.string.achievement_eagle_eye_three
        Achievement.Tier.FOUR -> R.string.achievement_eagle_eye_four
        Achievement.Tier.FIVE -> R.string.achievement_eagle_eye_five
    }
    Achievement.KNIGHTS_PATH -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_knights_path_one
        Achievement.Tier.TWO -> R.string.achievement_knights_path_two
        Achievement.Tier.THREE -> R.string.achievement_knights_path_three
        Achievement.Tier.FOUR -> R.string.achievement_knights_path_four
        Achievement.Tier.FIVE -> R.string.achievement_knights_path_five
    }
    Achievement.PUZZLE_ADDICT -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_puzzle_addict_one
        Achievement.Tier.TWO -> R.string.achievement_puzzle_addict_two
        Achievement.Tier.THREE -> R.string.achievement_puzzle_addict_three
        Achievement.Tier.FOUR -> R.string.achievement_puzzle_addict_four
        Achievement.Tier.FIVE -> R.string.achievement_puzzle_addict_five
    }
    Achievement.TIME_INVESTED -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_time_invested_one
        Achievement.Tier.TWO -> R.string.achievement_time_invested_two
        Achievement.Tier.THREE -> R.string.achievement_time_invested_three
        Achievement.Tier.FOUR -> R.string.achievement_time_invested_four
        Achievement.Tier.FIVE -> R.string.achievement_time_invested_five
    }
    Achievement.DAILY_GRINDER -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_daily_grinder_one
        Achievement.Tier.TWO -> R.string.achievement_daily_grinder_two
        Achievement.Tier.THREE -> R.string.achievement_daily_grinder_three
        Achievement.Tier.FOUR -> R.string.achievement_daily_grinder_four
        Achievement.Tier.FIVE -> R.string.achievement_daily_grinder_five
    }
    Achievement.CONSISTENCY_KING -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_consistency_king_one
        Achievement.Tier.TWO -> R.string.achievement_consistency_king_two
        Achievement.Tier.THREE -> R.string.achievement_consistency_king_three
        Achievement.Tier.FOUR -> R.string.achievement_consistency_king_four
        Achievement.Tier.FIVE -> R.string.achievement_consistency_king_five
    }
    Achievement.BOARD_VISION -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_board_vision_one
        Achievement.Tier.TWO -> R.string.achievement_board_vision_two
        Achievement.Tier.THREE -> R.string.achievement_board_vision_three
        Achievement.Tier.FOUR -> R.string.achievement_board_vision_four
        Achievement.Tier.FIVE -> R.string.achievement_board_vision_five
    }
    Achievement.RUSH_SOLVER -> when (tier) {
        Achievement.Tier.ONE -> R.string.achievement_rush_solver_one
        Achievement.Tier.TWO -> R.string.achievement_rush_solver_two
        Achievement.Tier.THREE -> R.string.achievement_rush_solver_three
        Achievement.Tier.FOUR -> R.string.achievement_rush_solver_four
        Achievement.Tier.FIVE -> R.string.achievement_rush_solver_five
    }
}
