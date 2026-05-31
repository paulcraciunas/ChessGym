package com.paulcraciunas.screens.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable

@Composable
fun <S> AnimatedBoard(
    targetState: S,
    contentKey: (targetState: S) -> Any? = { it },
    board: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    val enableAnimations = LocalUiSettings.current.enableAnimations
    AnimatedContent(
        targetState = targetState,
        contentKey = contentKey,
        transitionSpec = {
            if (enableAnimations) {
                (slideInHorizontally { width -> width })
                    .togetherWith(slideOutHorizontally { width -> -width })
            } else {
                EnterTransition.None togetherWith ExitTransition.None
            }
        },
        label = "BoardTransition"
    ) { newState ->
        board(newState)
    }
}

@Composable
fun <S> AnimatedControls(
    targetState: S,
    contentKey: (targetState: S) -> Any? = { it },
    controls: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    val enableAnimations = LocalUiSettings.current.enableAnimations
    AnimatedContent(
        targetState = targetState,
        contentKey = contentKey,
        transitionSpec = {
            if (enableAnimations) {
                (slideInVertically { height -> height } + fadeIn())
                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
            } else {
                EnterTransition.None togetherWith ExitTransition.None
            }
        },
        label = "ControlsAnimation"
    ) { newState ->
        controls(newState)
    }
}
