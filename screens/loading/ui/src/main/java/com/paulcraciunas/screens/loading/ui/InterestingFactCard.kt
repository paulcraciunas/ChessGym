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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.FactBlock
import com.paulcraciunas.screens.common.design.components.FactBlockStyle
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun InterestingFactCard(
    factIndex: Int,
    modifier: Modifier = Modifier,
) {
    val titles = stringArrayResource(R.array.loading_fact_titles)
    val bodies = stringArrayResource(R.array.loading_fact_bodies)
    val index = factIndex % titles.size

    AnimatedContent(
        targetState = index,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(200))
        },
        label = "fact_animation",
        modifier = modifier.padding(top = Design.dimensions.spacing.xgut),
    ) { currentIndex ->
        FactBlock(
            title = titles[currentIndex],
            items = listOf(bodies[currentIndex]),
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
            InterestingFactCard(factIndex = 0)
            InterestingFactCard(factIndex = 1)
            InterestingFactCard(factIndex = 2)
        }
    }
}
