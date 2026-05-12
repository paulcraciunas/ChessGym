package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

/**
 * Bordered, rounded-square icon container used in dashboard cards
 * and other contexts requiring a framed icon.
 */
@Composable
fun IconBadge(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val shape = Design.shapes.card
    Box(
        modifier = modifier
            .size(Design.dimensions.sizes.avatar)
            .clip(shape)
            .border(borderSoft(), shape)
            .background(
                if (enabled) Design.colors.primarySoft
                else Design.colors.primarySoftDisabled
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun IconBadge(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = if (enabled) Design.colors.primary else Design.colors.primaryDisabled,
) {
    IconBadge(modifier = modifier, enabled = enabled) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Design.dimensions.sizes.navBarIconHeight),
        )
    }
}

@Composable
fun IconBadge(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = if (enabled) Design.colors.primary else Design.colors.primaryDisabled,
) {
    IconBadge(modifier = modifier, enabled = enabled) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Design.dimensions.sizes.navBarIconHeight),
        )
    }
}

@Preview("IconBadge")
@Preview("IconBadge (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun IconBadgePreview() {
    ChessGymTheme {
        IconBadge(imageVector = Icons.Default.Star)
    }
}

@Preview("IconBadge disabled")
@Composable
private fun IconBadgeDisabledPreview() {
    ChessGymTheme {
        IconBadge(imageVector = Icons.Default.Star, enabled = false)
    }
}
