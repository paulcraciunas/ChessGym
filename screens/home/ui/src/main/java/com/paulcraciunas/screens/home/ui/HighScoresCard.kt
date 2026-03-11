package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState

@Composable
internal fun HighScoresCard(
    title: String,
    stats: HomeUiState.Stats,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "chevron_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .animateContentSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag { HomeScreenTags.HIGH_SCORES_CARD }
                    .clickable(
                        role = Role.Button,
                        onClick = { isExpanded = !isExpanded }
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) {
                        stringResource(R.string.collapse_high_scores)
                    } else {
                        stringResource(R.string.expand_high_scores)
                    },
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                        .testTag { HomeScreenTags.HIGH_SCORES_EXPANDED_CONTENT },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRow(
                        label = stringResource(R.string.user_stat_high_score_puzzles_rush),
                        value = stats.bestPuzzleRushScore.toString()
                    )
                    StatRow(
                        label = stringResource(R.string.user_stat_high_score_puzzle_streak),
                        value = stats.bestPuzzleStreakScore.toString()
                    )
                    StatRow(
                        label = stringResource(R.string.user_stat_high_score_blind_mode),
                        value = stats.bestBlindModeScore.toString()
                    )
                    StatRow(
                        label = stringResource(R.string.user_stat_high_score_find_the_square),
                        value = stats.bestFindTheSquareScore.toString()
                    )
                    StatRow(
                        label = stringResource(R.string.user_stat_high_score_move_the_piece),
                        value = stats.bestMoveThePieceScore.toString()
                    )
                }
            }
        }
    }
}

@Preview("HighScoresCard")
@Preview("HighScoresCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HighScoresCardPreview() {
    ChessGymTheme {
        HighScoresCard(
            title = "High Scores",
            stats = HomeUiState.Stats(
                puzzlesPlayed = 142,
                puzzlesSolved = 108,
                currentRating = 1547,
                bestRating = 1623,
                bestPuzzleRushScore = 23,
                bestPuzzleStreakScore = 15,
                bestFindTheSquareScore = 42,
                bestMoveThePieceScore = 18,
                bestBlindModeScore = 8,
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
