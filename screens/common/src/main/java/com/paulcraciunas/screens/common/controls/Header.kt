package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

enum class HeaderAlign { Beginning, Centered }
enum class HeaderSize { Default, Large }

@Composable
fun Header(
    @StringRes eyebrowRes: Int,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    modifier: Modifier = Modifier,
    align: HeaderAlign = HeaderAlign.Beginning,
    size: HeaderSize = HeaderSize.Default,
) {
    val titleStyle = size.typography()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        horizontalAlignment = align.alignment()
    ) {
        Eyebrow(text = stringResource(eyebrowRes))
        Text(
            text = stringResource(titleRes),
            style = titleStyle,
            color = Design.colors.ink,
        )
        Text(
            text = stringResource(subtitleRes),
            style = Design.typography.bodyLarge,
            color = Design.colors.inkSoft,
        )
    }
}

@Composable
fun Header(
    @StringRes eyebrowRes: Int,
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    align: HeaderAlign = HeaderAlign.Beginning,
    size: HeaderSize = HeaderSize.Default,
) {
    val titleStyle = size.typography()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        horizontalAlignment = align.alignment()
    ) {
        Eyebrow(text = stringResource(eyebrowRes))
        Text(
            text = stringResource(titleRes),
            style = titleStyle,
            color = Design.colors.ink,
        )
    }
}

@Composable
fun Header(
    eyebrow: String,
    title: AnnotatedString,
    subtitle: String,
    modifier: Modifier = Modifier,
    align: HeaderAlign = HeaderAlign.Beginning,
    size: HeaderSize = HeaderSize.Default,
) {
    val titleStyle = size.typography()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        horizontalAlignment = align.alignment()
    ) {
        Eyebrow(text = eyebrow)
        Text(
            text = title,
            style = titleStyle,
            color = Design.colors.ink,
        )
        Text(
            text = subtitle,
            style = Design.typography.bodyLarge,
            color = Design.colors.inkSoft,
        )
    }
}

@Composable
@Stable
private fun HeaderAlign.alignment(): Alignment.Horizontal = when (this) {
    HeaderAlign.Beginning -> Alignment.Start
    HeaderAlign.Centered -> Alignment.CenterHorizontally
}

@Composable
@Stable
private fun HeaderSize.typography() = when (this) {
    HeaderSize.Default -> Design.typography.headlineLarge
    HeaderSize.Large -> Design.typography.displayMedium
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

@Preview("LargeHeader")
@Preview("LargeHeader (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun LargeHeaderCenteredPreview() {
    ChessGymTheme {
        Header(
            eyebrowRes = R.string.puzzle_dashboard_eyebrow,
            titleRes = R.string.puzzle_dashboard_title,
            subtitleRes = R.string.puzzle_dashboard_subtitle,
            align = HeaderAlign.Centered,
            size = HeaderSize.Large
        )
    }
}
