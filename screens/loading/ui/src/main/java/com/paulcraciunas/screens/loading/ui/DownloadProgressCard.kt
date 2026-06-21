package com.paulcraciunas.screens.loading.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.Footer
import com.paulcraciunas.screens.common.controls.Header
import com.paulcraciunas.screens.common.controls.HeaderAlign
import com.paulcraciunas.screens.common.controls.HeaderSize
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.IconBadge
import com.paulcraciunas.screens.common.design.components.IconBorderType
import com.paulcraciunas.screens.common.design.components.IconSize
import com.paulcraciunas.screens.common.design.components.IconStyle
import com.paulcraciunas.screens.common.design.components.IconTintMode
import com.paulcraciunas.screens.common.design.components.IconTintType
import com.paulcraciunas.screens.common.design.components.LinearProgress
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.TextBadge
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.loading.vm.LoadingState

@Composable
internal fun DownloadProgressCard(
    progress: LoadingState.Downloading.Progress,
    modifier: Modifier = Modifier,
    factIndex: Int = 0,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Design.colors.bg)
            .navigationBarsPadding()
            .padding(horizontal = Design.dimensions.spacing.section),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Header(
                eyebrowRes = R.string.landing_download_eyebrow,
                titleRes = R.string.landing_download_content_title,
                align = HeaderAlign.Beginning,
                size = HeaderSize.Large
            )
            ChessGymSpacer(size = SpacerSize.HUGE)
            Column(
                verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.section),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ProgressStep(
                    stepNumber = 1,
                    label = stringResource(R.string.landing_download_progress),
                    description = stringResource(R.string.landing_download_subtitle),
                    progress = progress.download,
                )

                ProgressStep(
                    stepNumber = 2,
                    label = stringResource(R.string.landing_unpack_progress),
                    description = stringResource(R.string.landing_unpack_subtitle),
                    progress = progress.unpack,
                )
            }
            InterestingFactCard(factIndex = factIndex)
            ChessGymSpacer(size = SpacerSize.SECTION)
            Footer()
        }
    }
}

@Composable
private fun ProgressStep(
    stepNumber: Int,
    label: String,
    description: String,
    progress: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (progress == 100) {
                IconBadge(
                    imageVector = Icons.Default.Check,
                    style = IconStyle.Circle,
                    borderType = IconBorderType.None,
                    tint = IconTintType.Success,
                    tintMode = IconTintMode.Reversed,
                    size = IconSize.Small
                )
            } else {
                TextBadge(
                    text = "$stepNumber",
                    size = IconSize.Small,
                    textStyle = Design.typography.labelLarge,
                )
            }
            Text(
                text = label,
                style = Design.typography.titleMedium,
                color = Design.colors.ink,
                modifier = Modifier.weight(1f)
            )

            AnimatedContent(
                targetState = progress == 100,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300))) togetherWith
                            (fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150)))
                },
                label = "progress_animation",
            ) { isComplete ->
                Text(
                    text = if (isComplete) stringResource(R.string.generic_done) else "${progress}%",
                    style = Design.typography.titleMedium,
                    color = if (isComplete) Design.colors.success else Design.colors.ink,
                )
            }
        }

        LinearProgress(
            progress = progress / 100f,
            color = if (progress == 100) Design.colors.success else Design.colors.primary,
            modifier = Modifier.fillMaxWidth(),
        )

        if (progress < 100) {
            Text(
                text = description,
                style = Design.typography.bodyMedium,
                color = Design.colors.inkSoft,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Preview("DownloadProgressCard")
@Preview("DownloadProgressCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DownloadProgressCardPreview() {
    ChessGymTheme {
        DownloadProgressCard(
            progress = LoadingState.Downloading.Progress(
                download = 100,
                unpack = 45,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressStepPreview() {
    ChessGymTheme {
        Column(
            modifier = Modifier.padding(Design.dimensions.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxl),
        ) {
            ProgressStep(
                stepNumber = 1,
                label = "Downloading",
                description = "Downloading puzzle database...",
                progress = 100,
            )
            ProgressStep(
                stepNumber = 2,
                label = "Unpacking",
                description = "Unpacking puzzle database...",
                progress = 66,
            )
        }
    }
}
