package com.paulcraciunas.screens.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.home.vm.HomeUiState

@Composable
internal fun EventStats(
    event: HomeUiState.HistoryEvent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (event) {
            is HomeUiState.HistoryEvent.PuzzleRushEvent -> {
                StatChip(
                    label = pluralStringResource(R.plurals.home_timeline_stat_run, event.runs),
                    value = event.runs.toString()
                )
                StatChip(
                    label = stringResource(R.string.home_timeline_stat_score),
                    value = event.highScore.toString()
                )
            }
            is HomeUiState.HistoryEvent.BoardVizEvent -> {
                StatChip(
                    label = pluralStringResource(R.plurals.home_timeline_stat_session, event.runs),
                    value = event.runs.toString()
                )
            }
            is HomeUiState.HistoryEvent.RatedPuzzleEvent -> {
                RatingChangeChip(ratingChange = event.ratingChange)
                StatChip(
                    label = pluralStringResource(R.plurals.home_timeline_stat_puzzle, event.count),
                    value = event.count.toString()
                )
            }
            is HomeUiState.HistoryEvent.BlindModeEvent -> {
                StatChip(
                    label = stringResource(R.string.home_timeline_stat_moves),
                    value = event.completedMoves.toString()
                )
                StatChip(
                    label = pluralStringResource(R.plurals.home_timeline_stat_session, event.runs),
                    value = event.runs.toString()
                )
            }
        }
    }
}

@Composable
private fun RatingChangeChip(
    ratingChange: Int,
    modifier: Modifier = Modifier
) {
    val isPositive = ratingChange > 0
    val backgroundColor = if (isPositive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    }
    val contentColor = if (isPositive) {
        Color(0xFF4CAF50)
    } else {
        MaterialTheme.colorScheme.error
    }

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (isPositive) "+$ratingChange" else ratingChange.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
