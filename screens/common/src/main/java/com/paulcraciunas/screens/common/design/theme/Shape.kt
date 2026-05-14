package com.paulcraciunas.screens.common.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import com.paulcraciunas.screens.common.design.theme.walnut.WalnutRadii
import com.paulcraciunas.screens.common.design.theme.walnut.WalnutShapes
import com.paulcraciunas.screens.common.design.theme.walnut.WalnutStandardShapes

@Immutable
data class ChessGymShape(
    val soft: RoundedCornerShape,
    val card: RoundedCornerShape,
    val cardCompact: RoundedCornerShape,
    val pill: RoundedCornerShape,
    val circle: RoundedCornerShape,
    val navBar: RoundedCornerShape,
    val button: RoundedCornerShape,
    val borderAccent: RoundedCornerShape,
    val buttonOutline: RoundedCornerShape,
    val buttonPill: RoundedCornerShape,
)

@Immutable
data class ChessGymRadius(
    val none: Dp,
    val xxs: Dp,
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp,
    val pill: Dp,
)

internal val LocalChessGymShapes = staticCompositionLocalOf<ChessGymShape> {
    error("ChessGymTheme not provided. Wrap your content in ChessGymTheme { ... }.")
}

internal val LocalChessGymRadii = staticCompositionLocalOf<ChessGymRadius> {
    error("ChessGymTheme not provided. Wrap your content in ChessGymTheme { ... }.")
}

internal fun getChessGymShapes(): ChessGymShape = WalnutShapes
internal fun getStandardShapes(): Shapes = WalnutStandardShapes
internal fun getChessGymRadii(): ChessGymRadius = WalnutRadii
