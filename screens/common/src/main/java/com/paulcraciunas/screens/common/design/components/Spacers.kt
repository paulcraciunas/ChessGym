package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paulcraciunas.screens.common.design.theme.Design

enum class SpacerSize {
    XSMALL,
    SMALL,
    DEFAULT,
    MEDIUM,
    LARGE,
    XXLARGE,
    HUGE,
    SECTION,
}

@Composable
fun ChessGymSpacer(
    modifier: Modifier = Modifier,
    size: SpacerSize = SpacerSize.DEFAULT,
) {
    Spacer(
        modifier = modifier.size(
            when (size) {
                SpacerSize.XSMALL -> Design.dimensions.spacing.xxs
                SpacerSize.SMALL -> Design.dimensions.spacing.xs
                SpacerSize.DEFAULT -> Design.dimensions.spacing.sm
                SpacerSize.MEDIUM -> Design.dimensions.spacing.md
                SpacerSize.LARGE -> Design.dimensions.spacing.lg
                SpacerSize.XXLARGE -> Design.dimensions.spacing.xxl
                SpacerSize.HUGE -> Design.dimensions.spacing.xgut
                SpacerSize.SECTION -> Design.dimensions.spacing.xsection
            }
        )
    )
}
