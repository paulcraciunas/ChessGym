package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

enum class IconStyle { Card, Circle }
enum class IconBorderType { None, Soft, Hard }
enum class IconTintType { Normal, Accent, Danger, Success }
enum class IconTintMode { Normal, Reversed }
enum class IconSize { Small, Normal, Large }

@Composable
fun TextBadge(
    text: String,
    modifier: Modifier = Modifier,
    style: IconStyle = IconStyle.Circle,
    borderType: IconBorderType = IconBorderType.None,
    tint: IconTintType = IconTintType.Accent,
    size: IconSize = IconSize.Normal,
) {
    Text(
        text = text,
        color = tint.color(),
        modifier = modifier
            .badgeLayout(
                size = size.dp() * 2,
                style = style,
                border = borderType.borderStroke(),
                enabled = true,
                background = null
            )
            .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun IconBadge(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    style: IconStyle,
    borderType: IconBorderType,
    tint: IconTintType,
    tintMode: IconTintMode = IconTintMode.Normal,
    size: IconSize = IconSize.Large,
) {
    val tintColor = tint.color()
    val sizeDp = size.dp()

    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = if (tintMode == IconTintMode.Reversed) Color.White else tintColor,
        modifier = modifier
            .badgeLayout(
                size = sizeDp * 2,
                style = style,
                border = borderType.borderStroke(),
                enabled = true,
                background = if (tintMode == IconTintMode.Reversed) tintColor else null
            )
            .padding(sizeDp / 2)
    )
}

@Composable
fun IconBadge(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = if (enabled) Design.colors.primary else Design.colors.primaryDisabled,
) {
    val totalSize = Design.dimensions.sizes.avatar
    val iconSize = Design.dimensions.sizes.navBarIconHeight
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .badgeLayout(
                size = totalSize,
                style = IconStyle.Card,
                border = Design.colors.softBorderStroke,
                enabled = enabled,
                background = null
            )
            .padding((totalSize - iconSize) / 2)
    )
}

@Composable
fun IconBadge(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = if (enabled) Design.colors.primary else Design.colors.primaryDisabled,
) {
    val totalSize = Design.dimensions.sizes.avatar
    val iconSize = Design.dimensions.sizes.navBarIconHeight
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .badgeLayout(
                size = totalSize,
                style = IconStyle.Card,
                border = Design.colors.softBorderStroke,
                enabled = enabled,
                background = null
            )
            .padding((totalSize - iconSize) / 2)
    )
}

@Composable
private fun Modifier.badgeLayout(
    size: Dp,
    style: IconStyle,
    border: BorderStroke,
    enabled: Boolean,
    background: Color?,
): Modifier = this
    .size(size)
    .clip(
        when (style) {
            IconStyle.Card -> Design.shapes.card
            IconStyle.Circle -> Design.shapes.circle
        }
    )
    .border(
        border,
        when (style) {
            IconStyle.Card -> Design.shapes.card
            IconStyle.Circle -> Design.shapes.circle
        }
    )
    .background(
        when {
            background != null -> background
            enabled -> Design.colors.primarySoft
            else -> Design.colors.primarySoftDisabled
        }
    )

@Composable
@Stable
private fun IconBorderType.borderStroke(): BorderStroke = when (this) {
    IconBorderType.None -> Design.colors.borderNone
    IconBorderType.Soft -> Design.colors.softBorderStroke
    IconBorderType.Hard -> Design.colors.primaryBorderStroke
}

@Composable
@Stable
private fun IconTintType.color(): Color = when (this) {
    IconTintType.Normal -> Design.colors.primary
    IconTintType.Accent -> Design.colors.accent
    IconTintType.Danger -> Design.colors.danger
    IconTintType.Success -> Design.colors.success
}

@Composable
@Stable
private fun IconSize.dp(): Dp = when (this) {
    IconSize.Small -> Design.dimensions.sizes.iconSmall
    IconSize.Normal -> Design.dimensions.sizes.icon
    IconSize.Large -> Design.dimensions.sizes.navBarIconHeight
}

@Preview("IconBadge")
@Preview("IconBadge (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun IconBadgePreview() {
    ChessGymTheme {
        IconBadge(imageVector = Icons.Default.Star)
    }
}

@Preview("IconBadge")
@Preview("IconBadge (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TextBadgePreview() {
    ChessGymTheme {
        TextBadge(text = "1")
    }
}

@Preview("IconBadge disabled")
@Composable
private fun IconBadgeDisabledPreview() {
    ChessGymTheme {
        IconBadge(imageVector = Icons.Default.Star, enabled = false)
    }
}

@Preview("IconBadge CircleSoftDanger", showBackground = true)
@Composable
private fun IconBadgeCircleSoftDangerPreview() {
    ChessGymTheme {
        IconBadge(
            imageVector = Icons.Default.Star,
            style = IconStyle.Circle,
            borderType = IconBorderType.Soft,
            tint = IconTintType.Danger,
        )
    }
}
