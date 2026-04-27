package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme

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
fun ClockCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = DashboardCard(
    isEnabled = true,
    icon = { ResIcon(icon = R.drawable.clock_icon) },
    title = R.string.tools_clock_title,
    description = R.string.tools_clock_description,
    startContentDescription = R.string.tools_mode_start,
    highlight = null,
    onClick = onClick,
    modifier = modifier
)

@Composable
fun AnalysisCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = DashboardCard(
    isEnabled = true,
    icon = { ResIcon(icon = R.drawable.icon_analysis) },
    title = R.string.tools_analysis_title,
    description = R.string.tools_analysis_description,
    startContentDescription = R.string.tools_mode_start,
    highlight = null,
    onClick = onClick,
    modifier = modifier
)

@Composable
fun ImportGameCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = DashboardCard(
    isEnabled = true,
    icon = { ResIcon(icon = R.drawable.puzzle_icon) },
    title = R.string.tools_import_title,
    description = R.string.tools_import_description,
    startContentDescription = R.string.tools_mode_start,
    highlight = null,
    onClick = onClick,
    modifier = modifier
)

@Composable
fun MoveThePieceCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) = DashboardCard(
    isEnabled = true,
    icon = { ResIcon(icon = R.drawable.knight_white) },
    title = R.string.boardvis_move_piece_title,
    description = R.string.boardvis_move_piece_description,
    startContentDescription = R.string.boardvis_mode_start,
    highlight = null,
    onClick = onClick,
    modifier = modifier
)

@Preview("FindTheSquareCard")
@Preview("FindTheSquareCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FindTheSquareCardPreview() {
    ChessGymTheme {
        FindTheSquareCard(
            highScore = 35,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("FindTheSquareCard - No high score")
@Composable
private fun FindTheSquareCardNoHighScorePreview() {
    ChessGymTheme {
        FindTheSquareCard(
            highScore = 0,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("MoveThePieceCard")
@Preview("MoveThePieceCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun MoveThePieceCardPreview() {
    ChessGymTheme {
        MoveThePieceCard(
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("RatedPuzzleCard")
@Preview("RatedPuzzleCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun RatedPuzzleCardPreview() {
    ChessGymTheme {
        RatedPuzzleCard(
            userRating = 1547,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}


@Preview("PuzzleRushCard")
@Preview("PuzzleRushCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleRushCardPreview() {
    ChessGymTheme {
        PuzzleRushCard(
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("PuzzleRushCard new streak")
@Preview("PuzzleRushCard new streak (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun NewPuzzleStreakCardPreview() {
    ChessGymTheme {
        PuzzleStreakCard(
            currentStreak = 0,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("PuzzleRushCard continue streak")
@Preview("PuzzleRushCard continue streak (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ContinuePuzzleStreakCardPreview() {
    ChessGymTheme {
        PuzzleStreakCard(
            currentStreak = 42,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("FailedPuzzlesCard - With Failed Puzzles")
@Preview("FailedPuzzlesCard - With Failed Puzzles (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FailedPuzzlesCardWithFailuresPreview() {
    ChessGymTheme {
        FailedPuzzlesCard(
            failedCount = 12,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("FailedPuzzlesCard - No Failed Puzzles")
@Composable
private fun FailedPuzzlesCardEmptyPreview() {
    ChessGymTheme {
        FailedPuzzlesCard(
            failedCount = 0,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("ClockCard")
@Preview("ClockCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ClockCardPreview() {
    ChessGymTheme {
        ClockCard(
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("AnalysisCard")
@Preview("AnalysisCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AnalysisCardPreview() {
    ChessGymTheme {
        AnalysisCard(
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("ImportGameCard")
@Preview("ImportGameCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ImportGameCardPreview() {
    ChessGymTheme {
        ImportGameCard(
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
