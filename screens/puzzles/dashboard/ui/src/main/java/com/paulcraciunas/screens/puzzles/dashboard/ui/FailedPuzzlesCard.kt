package com.paulcraciunas.screens.puzzles.dashboard.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun FailedPuzzlesCard(
    failedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = failedCount > 0
    val alpha = if (isEnabled) 1f else 0.6f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f * alpha),
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = alpha))
            .then(
                if (isEnabled) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = if (isEnabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.puzzle_mode_failed_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Text(
                    text = stringResource(R.string.puzzle_mode_failed_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
                if (failedCount > 0) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.puzzle_mode_failed_count,
                            failedCount,
                            failedCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.puzzle_mode_failed_none_available),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                    )
                }
            }

            // Arrow - only show if enabled
            if (isEnabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.puzzle_mode_start),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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
