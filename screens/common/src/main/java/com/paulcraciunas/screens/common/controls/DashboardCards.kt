package com.paulcraciunas.screens.common.controls

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R

object DashboardCardScope {
    @Composable
    fun FindTheSquareCard(
        highScore: Int,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) = DashboardCard(
        isEnabled = true,
        icon = { ResIcon(icon = R.drawable.board_visualization_icon) },
        title = R.string.boardvis_find_square_title,
        description = R.string.boardvis_find_square_description,
        startContentDescription = R.string.boardvis_mode_start,
        highlight = if (highScore > 0) stringResource(R.string.boardvis_find_square_high_score, highScore) else null,
        onClick = onClick,
        modifier = modifier
    )

    @Composable
    fun FailedPuzzlesCard(
        failedCount: Int,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) = DashboardCard(
        isEnabled = failedCount > 0,
        icon = { VectorIcon(iconVector = Icons.Outlined.Refresh) },
        title = R.string.puzzle_mode_failed_title,
        description = R.string.puzzle_mode_failed_description,
        startContentDescription = R.string.puzzle_mode_start,
        highlight = if (failedCount > 0) pluralStringResource(
            R.plurals.puzzle_mode_failed_count,
            failedCount,
            failedCount
        ) else stringResource(R.string.puzzle_mode_failed_none_available),
        onClick = onClick,
        modifier = modifier
    )

    @Composable
    fun PuzzleRushCard(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) = DashboardCard(
        isEnabled = true,
        icon = { ResIcon(icon = R.drawable.puzzle_rush_icon) },
        title = R.string.puzzle_mode_rush_title,
        description = R.string.puzzle_mode_rush_description,
        startContentDescription = R.string.puzzle_mode_start,
        highlight = null,
        onClick = onClick,
        modifier = modifier
    )

    @Composable
    fun PuzzleStreakCard(
        currentStreak: Int,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) = DashboardCard(
        isEnabled = true,
        icon = { ResIcon(icon = R.drawable.puzzle_rush_icon) },
        title = R.string.puzzle_mode_streak_title,
        description = R.string.puzzle_mode_streak_description,
        startContentDescription = R.string.puzzle_mode_start,
        highlight = if (currentStreak == 0) stringResource(R.string.puzzle_mode_streak_new) else stringResource(
            R.string.puzzle_mode_streak_continue,
            currentStreak
        ),
        onClick = onClick,
        modifier = modifier
    )

    @Composable
    fun RatedPuzzleCard(
        userRating: Int,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) = DashboardCard(
        isEnabled = true,
        icon = { ResIcon(icon = R.drawable.puzzle_icon) },
        title = R.string.puzzle_mode_rated_title,
        description = R.string.puzzle_mode_rated_description,
        startContentDescription = R.string.puzzle_mode_start,
        highlight = stringResource(R.string.puzzle_mode_rated_rating, userRating),
        onClick = onClick,
        modifier = modifier
    )
}
