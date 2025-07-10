package com.paulcraciunas.screens.puzzles.rated.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun FinishedPuzzleControls(
    ratingChange: Int,
    modifier: Modifier = Modifier,
    onNavigateToStart: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateNext: () -> Unit = {},
    onNavigateToEnd: () -> Unit = {},
    onPlayNext: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Finished state: rating change + navigation controls + play next
        Text(
            text = if (ratingChange >= 0) "+$ratingChange" else "$ratingChange",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (ratingChange >= 0) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )

        // Navigation controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateToStart) {
                Icon(
                    painter = painterResource(R.drawable.keyboard_double_arrow_left),
                    contentDescription = "Go to start",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous move",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onNavigateNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next move",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onNavigateToEnd) {
                Icon(
                    painter = painterResource(R.drawable.keyboard_double_arrow_right),
                    contentDescription = "Go to end",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        FilledTonalButton(onClick = onPlayNext) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.rated_puzzle_next_description))
        }
    }
}


@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleControlsSolvedPreview() {
    ChessGymTheme {
        FinishedPuzzleControls(
            ratingChange = 15,
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleControlsFailedPreview() {
    ChessGymTheme {
        FinishedPuzzleControls(
            ratingChange = -12,
        )
    }
}
