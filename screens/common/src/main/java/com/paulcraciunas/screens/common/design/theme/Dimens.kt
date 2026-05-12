package com.paulcraciunas.screens.common.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import com.paulcraciunas.screens.common.design.theme.walnut.WalnutDimensions

@Immutable
data class ChessGymDimensions(
    val spacing: Spacing,
    val elevation: Elevation,
    val sizes: Sizes,
) {
    @Immutable
    data class Spacing(
        val none: Dp,
        val xxs: Dp,
        val xs: Dp,
        val s: Dp,
        val sm: Dp,
        val md: Dp,
        val lg: Dp,
        val xl: Dp,
        val xxl: Dp,
        val xxxl: Dp,
        val gut: Dp,
        val xgut: Dp,
        val section: Dp,
    )

    @Immutable
    data class Elevation(
        val none: Dp,
        val sm: Dp,
        val md: Dp,
        val lg: Dp,
        val nav: Dp,
    )

    @Immutable
    data class Sizes(
        val primaryButton: Dp,
        val pillButton: Dp,
        val outlineButton: Dp,
        val hitTarget: Dp,
        val iconButton: Dp,
        val icon: Dp,
        val iconSmall: Dp,
        val avatar: Dp,
        val medallion: Dp,
        val medallionText: Dp,
        val medallionLg: Dp,
        val navBarHeight: Dp,
        val navBarIconHeight: Dp,
        val appBarHeight: Dp,
        val toggleWidth: Dp,
        val toggleHeight: Dp,
        val toggleContent: Dp,
        val progressBar: Dp,
    )
}

internal val LocalChessGymDimensions = staticCompositionLocalOf<ChessGymDimensions> {
    error("ChessGymTheme not provided. Wrap your content in ChessGymTheme { ... }.")
}

internal fun getChessGymDimensions(): ChessGymDimensions = WalnutDimensions
