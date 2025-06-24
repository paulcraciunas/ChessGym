package com.paulcraciunas.screens.loading.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.common.theme.LoadingTheme
import com.paulcraciunas.screens.loading.vm.LoadingState

@Composable
internal fun DownloadProgressCard(
    progress: LoadingState.Downloading.Progress,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = LoadingTheme.dimensions.horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.sectionSpacing)
        ) {
            // App Title
            Text(
                text = stringResource(R.string.app_name),
                style = LoadingTheme.typography.appTitleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(LoadingTheme.dimensions.titleSpacing))

            // Main Title
            Text(
                text = stringResource(R.string.landing_download_content_title),
                style = LoadingTheme.typography.contentTitle,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(LoadingTheme.dimensions.contentSpacing))

            // Progress Items
            Column(
                verticalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.sectionSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                ModernProgressItem(
                    label = stringResource(R.string.landing_download_progress),
                    description = stringResource(R.string.landing_download_subtitle),
                    progress = progress.download
                )

                ModernProgressItem(
                    label = stringResource(R.string.landing_unpack_progress),
                    description = stringResource(R.string.landing_unpack_subtitle),
                    progress = progress.unpack
                )

                ModernProgressItem(
                    label = stringResource(R.string.landing_write_to_db_progress),
                    description = stringResource(R.string.landing_write_to_db_subtitle),
                    progress = progress.buildDb
                )
            }

            // Interesting facts during building phase
            AnimatedVisibility(
                visible = progress.buildDb in 1..99,
                enter = slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it / 2 }
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { -it / 2 }
                ) + fadeOut(animationSpec = tween(200))
            ) {
                InterestingFactCard(buildProgress = progress.buildDb)
            }

            Spacer(modifier = Modifier.height(LoadingTheme.dimensions.bottomSpacing))

            // App name at bottom
            Text(
                text = stringResource(R.string.app_name),
                style = LoadingTheme.typography.appTitleSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun ModernProgressItem(
    label: String,
    description: String,
    progress: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.itemSpacing)
    ) {
        // Header with label and percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label...",
                style = LoadingTheme.typography.progressLabel,
                color = MaterialTheme.colorScheme.onBackground
            )

            AnimatedContent(
                targetState = progress == 100,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300))) togetherWith
                            (fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150)))
                },
                label = "progress_animation"
            ) { isComplete ->
                Text(
                    text = if (isComplete) "✓" else "${progress}%",
                    style = LoadingTheme.typography.progressValue,
                    color = if (isComplete)
                        LoadingTheme.colors.success
                    else
                        MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(LoadingTheme.dimensions.progressBarHeight)
                .clip(RoundedCornerShape(LoadingTheme.dimensions.progressBarRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress / 100f)
                    .height(LoadingTheme.dimensions.progressBarHeight)
                    .clip(RoundedCornerShape(LoadingTheme.dimensions.progressBarRadius))
                    .background(
                        if (progress == 100)
                            LoadingTheme.colors.success
                        else
                            MaterialTheme.colorScheme.primary
                    )
            )
        }

        // Description (only show when not complete)
        if (progress < 100) {
            Text(
                text = description,
                style = LoadingTheme.typography.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
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
                unpack = 100,
                buildDb = 45
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ModernProgressItemPreview() {
    ChessGymTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ModernProgressItem(
                label = "Downloading",
                description = "Downloading puzzle database. This may take a few minutes, depending on the speed of your internet connection.",
                progress = 33
            )
            ModernProgressItem(
                label = "Unpacking",
                description = "Unpacking puzzle database. This may take a few minutes.",
                progress = 66
            )
            ModernProgressItem(
                label = "Building",
                description = "Building puzzle database. This may take a few minutes.",
                progress = 100
            )
        }
    }
}
