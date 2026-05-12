package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun DashboardHeader(
    @StringRes eyebrowRes: Int,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
    ) {
        Eyebrow(
            text = stringResource(eyebrowRes),
            color = Design.colors.primary,
        )
        Text(
            text = stringResource(titleRes),
            style = Design.typography.headlineLarge,
            color = Design.colors.ink,
        )
        Text(
            text = stringResource(subtitleRes),
            style = Design.typography.bodyLarge,
            color = Design.colors.inkSoft,
        )
    }
}

@Preview("DashboardHeader")
@Preview("DashboardHeader (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DashboardHeaderPreview() {
    ChessGymTheme {
        DashboardHeader(
            eyebrowRes = R.string.puzzle_dashboard_eyebrow,
            titleRes = R.string.puzzle_dashboard_title,
            subtitleRes = R.string.puzzle_dashboard_subtitle,
        )
    }
}
