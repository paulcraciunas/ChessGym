package com.paulcraciunas.screens.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class LoadingColors(
    val success: Color = Color(0xFF4CAF50),
    val error: Color = Color(0xFFE53E3E),
)

@Immutable
data class LoadingTypography(
    val appTitleLarge: TextStyle,
    val contentTitle: TextStyle,
    val progressLabel: TextStyle,
    val progressValue: TextStyle,
    val description: TextStyle,
    val factText: TextStyle,
    val appTitleSmall: TextStyle,
    val landingTitle: TextStyle,
    val landingDescription: TextStyle,
    val errorText: TextStyle,
    val buttonText: TextStyle,
    val dialogTitle: TextStyle,
    val dialogBody: TextStyle,
    val dialogButton: TextStyle,
)

@Immutable
data class LoadingDimensions(
    val horizontalPadding: Dp = 32.dp,
    val sectionSpacing: Dp = 32.dp,
    val itemSpacing: Dp = 12.dp,
    val progressBarHeight: Dp = 8.dp,
    val progressBarRadius: Dp = 4.dp,
    val factCardRadius: Dp = 12.dp,
    val factCardPadding: Dp = 16.dp,
    val factCardTopPadding: Dp = 24.dp,
    val factIconSpacing: Dp = 12.dp,
    val factIconTopPadding: Dp = 2.dp,
    val titleSpacing: Dp = 16.dp,
    val contentSpacing: Dp = 24.dp,
    val bottomSpacing: Dp = 48.dp,
    val landingContentSpacing: Dp = 32.dp,
    val errorSpacing: Dp = 24.dp,
    val buttonSpacing: Dp = 8.dp,
    val buttonRadius: Dp = 12.dp,
    val errorCardRadius: Dp = 8.dp,
    val errorCardPadding: Dp = 16.dp,
    val dialogIconSize: Dp = 32.dp,
    val dialogContentSpacing: Dp = 16.dp,
)

@Immutable
data class LoadingAlphas(
    val factCardBackground: Float = 0.6f,
    val factCardBorder: Float = 0.3f,
)

@Immutable
data class LoadingBorders(
    val factCardWidth: Dp = 1.dp,
)

@Composable
fun createLoadingTypography() = LoadingTypography(
    appTitleLarge = MaterialTheme.typography.headlineLarge.copy(
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold
    ),
    contentTitle = MaterialTheme.typography.headlineSmall.copy(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium
    ),
    progressLabel = MaterialTheme.typography.titleMedium.copy(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    ),
    progressValue = MaterialTheme.typography.titleMedium.copy(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    ),
    description = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp
    ),
    factText = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    appTitleSmall = MaterialTheme.typography.titleMedium.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    landingTitle = MaterialTheme.typography.headlineLarge.copy(
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold
    ),
    landingDescription = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp
    ),
    errorText = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    ),
    buttonText = MaterialTheme.typography.titleMedium.copy(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    ),
    dialogTitle = MaterialTheme.typography.headlineSmall.copy(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    dialogBody = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    dialogButton = MaterialTheme.typography.labelLarge.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
)

val LocalLoadingColors = staticCompositionLocalOf { LoadingColors() }
val LocalLoadingTypography = staticCompositionLocalOf<LoadingTypography> { error("LoadingTypography not provided") }
val LocalLoadingDimensions = staticCompositionLocalOf { LoadingDimensions() }
val LocalLoadingAlphas = staticCompositionLocalOf { LoadingAlphas() }
val LocalLoadingBorders = staticCompositionLocalOf { LoadingBorders() }

@Immutable
data class FullLoadingTheme(
    val colors: LoadingColors,
    val typography: LoadingTypography,
    val dimensions: LoadingDimensions,
    val alphas: LoadingAlphas,
    val borders: LoadingBorders,
)

val LoadingTheme: FullLoadingTheme
    @Composable
    @ReadOnlyComposable
    get() = FullLoadingTheme(
        colors = LocalLoadingColors.current,
        typography = LocalLoadingTypography.current,
        dimensions = LocalLoadingDimensions.current,
        alphas = LocalLoadingAlphas.current,
        borders = LocalLoadingBorders.current,
    )
