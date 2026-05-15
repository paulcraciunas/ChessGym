package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

enum class HeaderAlign { Beginning, Centered }

@Composable
fun Header(
    @StringRes eyebrowRes: Int,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    modifier: Modifier = Modifier,
    align: HeaderAlign = HeaderAlign.Beginning,
) {
    val textAlign = when (align) {
        HeaderAlign.Beginning -> TextAlign.Start
        HeaderAlign.Centered -> TextAlign.Center
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        horizontalAlignment = when (align) {
            HeaderAlign.Beginning -> Alignment.Start
            HeaderAlign.Centered -> Alignment.CenterHorizontally
        }
    ) {
        Eyebrow(text = stringResource(eyebrowRes))
        Text(
            text = stringResource(titleRes),
            style = Design.typography.headlineLarge,
            color = Design.colors.ink,
            textAlign = textAlign,
        )
        Text(
            text = stringResource(subtitleRes),
            style = Design.typography.bodyLarge,
            color = Design.colors.inkSoft,
            textAlign = textAlign,
        )
    }
}

@Preview("Header")
@Preview("Header (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HeaderPreview() {
    ChessGymTheme {
        Header(
            eyebrowRes = R.string.puzzle_dashboard_eyebrow,
            titleRes = R.string.puzzle_dashboard_title,
            subtitleRes = R.string.puzzle_dashboard_subtitle,
        )
    }
}

@Preview("Header")
@Preview("Header (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HeaderCenteredPreview() {
    ChessGymTheme {
        Header(
            eyebrowRes = R.string.puzzle_dashboard_eyebrow,
            titleRes = R.string.puzzle_dashboard_title,
            subtitleRes = R.string.puzzle_dashboard_subtitle,
            align = HeaderAlign.Centered
        )
    }
}
