package com.paulcraciunas.screens.common.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.paulcraciunas.screens.common.design.theme.pieces.ChessGymPieceSet
import com.paulcraciunas.screens.common.design.theme.pieces.LocalChessGymPieces

object Design {
    val colors: ChessGymColors
        @Composable @ReadOnlyComposable get() = LocalChessGymColors.current
    val typography: Typography
        @Composable @ReadOnlyComposable get() = LocalChessGymTypography.current
    val textStyles: DefaultTextStyles
        @Composable @ReadOnlyComposable get() = LocalChessGymTextStyles.current
    val dimensions: ChessGymDimensions
        @Composable @ReadOnlyComposable get() = LocalChessGymDimensions.current
    val shapes: ChessGymShape
        @Composable @ReadOnlyComposable get() = LocalChessGymShapes.current
    val radii: ChessGymRadius
        @Composable @ReadOnlyComposable get() = LocalChessGymRadii.current
    val pieces: ChessGymPieceSet
        @Composable @ReadOnlyComposable get() = LocalChessGymPieces.current
}
