package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymCard
import com.paulcraciunas.screens.common.design.components.IconBadge
import com.paulcraciunas.screens.common.design.theme.Design
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

    ChessGymCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(isEnabled) { onClick() }) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon(iconScope)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs)
            ) {
                Text(
                    text = stringResource(title),
                    style = Design.typography.titleLarge,
                    color = Design.colors.ink.copy(alpha = alpha)
                )
                Text(
                    text = stringResource(description),
                    style = Design.typography.bodyMedium,
                    color = Design.colors.inkSoft.copy(alpha = alpha)
                )
                highlight?.let {
                    Text(
                        text = it,
                        style = Design.typography.labelLarge,
                        color = Design.colors.primary.copy(alpha = alpha)
                    )
                }
            }

            if (isEnabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(startContentDescription),
                    tint = Design.colors.inkMuted,
                    modifier = Modifier.size(Design.dimensions.sizes.icon)
                )
            }
        }
    }
}

@Stable
internal class DashboardCardIconScope internal constructor(val isEnabled: Boolean) {
    @Composable
    fun ResIcon(@DrawableRes icon: Int) {
        IconBadge(
            iconRes = icon,
            enabled = isEnabled,
        )
    }

    @Composable
    fun VectorIcon(iconVector: ImageVector) {
        IconBadge(
            imageVector = iconVector,
            enabled = isEnabled,
        )
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
            highlight = stringResource(R.string.boardvis_high_score, 35),
            onClick = {},
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
        )
    }
}
