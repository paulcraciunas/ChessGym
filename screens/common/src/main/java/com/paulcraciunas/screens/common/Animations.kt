package com.paulcraciunas.screens.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import com.paulcraciunas.screens.data.BOARD_ANIMATION_DURATION_MS

@Composable
fun <S> AnimatedBoard(
    targetState: S,
    contentKey: (targetState: S) -> Any? = { it },
    board: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    val animationsEnabled = LocalUiSettings.current.enableAnimations
    AnimatedContent(
        targetState = targetState,
        contentKey = contentKey,
        transitionSpec = {
            if (!animationsEnabled) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(BOARD_ANIMATION_DURATION_MS)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(BOARD_ANIMATION_DURATION_MS)
                )
            }
        },
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
    val animationsEnabled = LocalUiSettings.current.enableAnimations
    AnimatedContent(
        targetState = targetState,
        contentKey = contentKey,
        transitionSpec = {
            if (!animationsEnabled) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                (slideInVertically { it } + fadeIn())
                    .togetherWith(slideOutVertically { -it } + fadeOut())
            }
        },
        label = "ControlsAnimation"
    ) { newState ->
        controls(newState)
    }
}
