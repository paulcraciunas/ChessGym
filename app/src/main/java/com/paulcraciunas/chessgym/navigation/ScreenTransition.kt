package com.paulcraciunas.chessgym.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design

/**
 * Fade-through-splash overlay.
 *
 * [progress] ranges from 0→2:
 * - 0→1: overlay fades from transparent to opaque (covering content)
 * - 1→2: overlay fades from opaque to transparent (revealing new content)
 * Logo appears near the midpoint.
 */
@Composable
internal fun FadeSplashOverlay(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val overlayColor = Design.colors.primarySoft
    val logoTint = Design.colors.primary

    val overlayAlpha = when {
        progress <= 1f -> progress
        else -> 2f - progress
    }.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(overlayAlpha)
            .background(overlayColor),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.knight_white),
            contentDescription = null,
            colorFilter = ColorFilter.tint(logoTint),
            modifier = Modifier
                .size(Design.dimensions.sizes.progressRing)
                .alpha(overlayAlpha),
        )
    }
}
