package com.paulcraciunas.screens.common.controls

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import com.paulcraciunas.global.resources.R

const val DEFAULT_PROGRESS_DURATION = 1000

@Composable
fun InfiniteProgressIndicator(
    modifier: Modifier = Modifier,
    duration: Int = DEFAULT_PROGRESS_DURATION
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing)
        )
    )

    Icon(
        painter = painterResource(id = R.drawable.board_visualization_icon),
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        contentDescription = "Loading",
        modifier = modifier.rotate(rotation) // Apply the animated rotation
    )
}
