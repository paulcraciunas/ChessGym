package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.extensions.alpha
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun DashboardCard(
    isEnabled: Boolean,
    icon: (@Composable DashboardCardIconScope.() -> Unit),
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes startContentDescription: Int,
    highlight: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconScope = remember { DashboardCardIconScope(isEnabled) }
    val alpha = isEnabled.alpha

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
                .padding(16.dp),
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
                icon(iconScope)
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Text(
                    text = stringResource(description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
                highlight?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                    )
                }
            }

            // Arrow - only show if enabled
            if (isEnabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(startContentDescription),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Stable
internal class DashboardCardIconScope internal constructor(val isEnabled: Boolean) {
    @Composable
    fun ResIcon(
        @DrawableRes icon: Int,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = iconTint(),
            modifier = Modifier.size(28.dp)
        )
    }

    @Composable
    fun VectorIcon(
        iconVector: ImageVector,
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = iconTint(),
            modifier = Modifier.size(28.dp)
        )
    }

    @Composable
    private fun iconTint() = if (isEnabled) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
    }
}

@Preview("DashboardCard")
@Preview("DashboardCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FindTheSquareCardPreview() {
    ChessGymTheme {
        DashboardCard(
            isEnabled = true,
            icon = { ResIcon(R.drawable.board_visualization_icon) },
            title = R.string.boardvis_find_square_title,
            description = R.string.boardvis_find_square_description,
            startContentDescription = R.string.boardvis_mode_start,
            highlight = stringResource(R.string.boardvis_find_square_high_score, 35),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("DashboardCard - No high score")
@Composable
private fun FindTheSquareCardNoHighScorePreview() {
    ChessGymTheme {
        DashboardCard(
            isEnabled = true,
            icon = { ResIcon(R.drawable.board_visualization_icon) },
            title = R.string.boardvis_find_square_title,
            description = R.string.boardvis_find_square_description,
            startContentDescription = R.string.boardvis_mode_start,
            highlight = null,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
