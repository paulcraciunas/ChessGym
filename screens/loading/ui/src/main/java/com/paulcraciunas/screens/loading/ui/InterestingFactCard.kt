package com.paulcraciunas.screens.loading.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.common.theme.LoadingTheme

@Composable
internal fun InterestingFactCard(
    buildProgress: Int,
    modifier: Modifier = Modifier
) {
    val fact = when {
        buildProgress < 33 -> stringResource(R.string.loading_fact_1)
        buildProgress < 66 -> stringResource(R.string.loading_fact_2)
        else -> stringResource(R.string.loading_fact_3)
    }

    AnimatedContent(
        targetState = fact,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
            fadeOut(animationSpec = tween(200))
        },
        label = "fact_animation",
        modifier = modifier.padding(top = LoadingTheme.dimensions.factCardTopPadding)
    ) { currentFact ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LoadingTheme.dimensions.factCardRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = LoadingTheme.alphas.factCardBackground))
                .border(
                    width = LoadingTheme.borders.factCardWidth,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = LoadingTheme.alphas.factCardBorder),
                    shape = RoundedCornerShape(LoadingTheme.dimensions.factCardRadius)
                )
                .padding(LoadingTheme.dimensions.factCardPadding)
        ) {
                        Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.factIconSpacing)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = LoadingTheme.dimensions.factIconTopPadding)
                )

                Text(
                    text = currentFact,
                    style = LoadingTheme.typography.factText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview("InterestingFactCard")
@Composable
private fun InterestingFactCardPreview() {
    ChessGymTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InterestingFactCard(buildProgress = 25)
            InterestingFactCard(buildProgress = 50)
            InterestingFactCard(buildProgress = 75)
        }
    }
}
