package com.paulcraciunas.screens.blindmode.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
internal fun MoveHistoryDisplay(
    moveHistory: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xxl, vertical = Design.dimensions.spacing.lg)
            .clip(Design.shapes.card)
            .background(Design.colors.surfaceAlt)
            .padding(Design.dimensions.spacing.lg),
    ) {
        Text(
            text = stringResource(R.string.blind_mode_moves),
            style = Design.typography.labelMedium,
            color = Design.colors.inkSoft,
        )
        ChessGymSpacer(size = SpacerSize.SMALL)
        Text(
            text = moveHistory,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis,
            style = Design.typography.bodyMedium,
            color = Design.colors.inkSoft,
            textAlign = TextAlign.Start,
        )
    }
}
