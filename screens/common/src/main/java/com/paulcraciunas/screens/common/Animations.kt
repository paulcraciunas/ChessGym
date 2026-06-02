package com.paulcraciunas.screens.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import com.paulcraciunas.screens.data.BOARD_ANIMATION_DURATION_MS

private val boardAnimation = slideInHorizontally(animationSpec = tween(BOARD_ANIMATION_DURATION_MS), initialOffsetX = { it })
    .togetherWith(slideOutHorizontally(animationSpec = tween(BOARD_ANIMATION_DURATION_MS), targetOffsetX = { -it }))
private val controlsAnimation = (slideInVertically { it } + fadeIn())
    .togetherWith(slideOutVertically { -it } + fadeOut())
private val noAnimation = EnterTransition.None togetherWith ExitTransition.None

@Composable
fun <S> AnimatedBoard(
    targetState: S,
    contentKey: (targetState: S) -> Any? = { it },
    board: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    val animation = if (LocalUiSettings.current.enableAnimations) boardAnimation else noAnimation
    AnimatedContent(
        targetState = targetState,
        contentKey = contentKey,
        transitionSpec = { animation },
        label = "BoardTransition"
    ) { newState ->
        this.board(newState)
    }
}

@Composable
fun <S> AnimatedControls(
    targetState: S,
    contentKey: (targetState: S) -> Any? = { it },
    controls: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    val animation = if (LocalUiSettings.current.enableAnimations) controlsAnimation else noAnimation
    AnimatedContent(
        targetState = targetState,
        contentKey = contentKey,
        transitionSpec = { animation },
        label = "ControlsAnimation"
    ) { newState ->
        controls(newState)
    }
}
