package com.paulcraciunas.screens.loading.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.FactBlock
import com.paulcraciunas.screens.common.design.components.FactBlockStyle
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun InterestingFactCard(
    downloadProgress: Int,
    modifier: Modifier = Modifier,
) {
    val fact = when {
        downloadProgress < 33 -> stringResource(R.string.loading_fact_1)
        downloadProgress < 66 -> stringResource(R.string.loading_fact_2)
        else -> stringResource(R.string.loading_fact_3)
    }

    AnimatedContent(
        targetState = fact,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(200))
        },
        label = "fact_animation",
        modifier = modifier.padding(top = Design.dimensions.spacing.xgut),
    ) { currentFact ->
        FactBlock(
            title = stringResource(R.string.loading_fact_eyebrow),
            items = listOf(currentFact),
            style = FactBlockStyle.Info,
        )
    }
}

@Preview("InterestingFactCard")
@Composable
private fun InterestingFactCardPreview() {
    ChessGymTheme {
        Column(
            modifier = Modifier.padding(Design.dimensions.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxl),
        ) {
            InterestingFactCard(downloadProgress = 25)
            InterestingFactCard(downloadProgress = 50)
            InterestingFactCard(downloadProgress = 75)
        }
    }
}
