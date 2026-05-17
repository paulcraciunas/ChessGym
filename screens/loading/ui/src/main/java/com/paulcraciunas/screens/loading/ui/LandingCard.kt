package com.paulcraciunas.screens.loading.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.Footer
import com.paulcraciunas.screens.common.controls.Header
import com.paulcraciunas.screens.common.controls.HeaderAlign
import com.paulcraciunas.screens.common.controls.HeaderSize
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.PrimaryPillButton
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.annotatedTextResource
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.loading.ui.elements.ErrorCard
import com.paulcraciunas.screens.loading.ui.elements.FeatureList
import com.paulcraciunas.screens.loading.ui.elements.TierSelector
import com.paulcraciunas.screens.loading.vm.DatabaseTier
import com.paulcraciunas.screens.loading.vm.LoadingState

@Composable
internal fun LandingCard(
    onDownload: () -> Unit,
    onTierSelected: (DatabaseTier) -> Unit,
    onDownloadConfirmation: (Boolean) -> Unit,
    onPermissionResponse: (Boolean) -> Unit,
    onCrashConsentResponse: (Boolean) -> Unit,
    state: LoadingState.Ready,
    modifier: Modifier = Modifier,
) {
    val canDownload = state.error != LoadingState.Error.NoInternet

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Design.colors.bg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Design.dimensions.spacing.xsection)
        ) {
            ChessboardPattern()
            KnightDecoration(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(40.dp)
                    .offset(x = 40.dp, y = 16.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Design.dimensions.spacing.section)
                .verticalScroll(rememberScrollState()),
        ) {
            Header(
                eyebrow = stringResource(R.string.landing_eyebrow),
                title = annotatedTextResource(id = R.string.landing_download_title),
                subtitle = stringResource(R.string.landing_download_explanation),
                align = HeaderAlign.Beginning,
                size = HeaderSize.Large
            )
            ChessGymSpacer(size = SpacerSize.HUGE)
            FeatureList()
            ChessGymSpacer(size = SpacerSize.LARGE)
            TierSelector(
                selectedTier = state.selectedTier,
                onTierSelected = onTierSelected,
            )
            ChessGymSpacer(size = SpacerSize.LARGE)
            PrimaryPillButton(
                text = stringResource(state.primaryButtonTextRes()),
                onClick = onDownload,
                enabled = canDownload,
                leadingIcon = if (state.selectedTier.isBundled) Icons.Default.Check else ImageVector.vectorResource(id = R.drawable.download_icon),
                modifier = Modifier.fillMaxWidth(),
            )
            ChessGymSpacer(size = SpacerSize.DEFAULT)
            Text(
                text = state.selectedTier.sizeHint(),
                style = Design.typography.bodySmall,
                color = Design.colors.inkMuted,
                textAlign = TextAlign.Center,
            )
            ChessGymSpacer(size = SpacerSize.LARGE)
            ErrorCard(error = state.error)
            ChessGymSpacer(size = SpacerSize.DEFAULT)
            Footer()
            ChessGymSpacer(size = SpacerSize.LARGE)
        }
    }

    when (state.dialog) {
        LoadingState.Dialog.CrashConsent -> CrashReportingConsentDialog(
            onAccepted = { onCrashConsentResponse(true) },
            onDeclined = { onCrashConsentResponse(false) },
        )
        LoadingState.Dialog.Download -> DownloadConfirmationDialog(
            onCancelled = { onDownloadConfirmation(false) },
            onConfirmed = { onDownloadConfirmation(true) },
        )
        LoadingState.Dialog.Permission -> PermissionDialog(onPermissionResponse)
        LoadingState.Dialog.None -> {}
    }
}

@Composable
private fun DatabaseTier.sizeHint(): String = when {
    isBundled -> stringResource(R.string.landing_size_hint_included)
    else -> stringResource(R.string.landing_size_hint_onetime, approximateSize)
}

private fun LoadingState.Ready.primaryButtonTextRes(): Int = when {
    error.isRetryable() -> R.string.generic_retry
    selectedTier.isBundled -> R.string.landing_get_started_button
    else -> R.string.landing_download_button
}

private fun LoadingState.Error.isRetryable(): Boolean = when (this) {
    LoadingState.Error.ConsentRequired,
    LoadingState.Error.DownloadFailed,
    LoadingState.Error.DecompressionFailed,
    LoadingState.Error.DatabaseWriteFailed,
    LoadingState.Error.GenericRuntime,
    -> true
    else -> false
}

@Preview("LandingCard - Normal")
@Preview("LandingCard - Normal (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun LandingCardPreview() {
    ChessGymTheme {
        LandingCard(
            onDownload = {},
            onTierSelected = {},
            onDownloadConfirmation = {},
            onPermissionResponse = {},
            onCrashConsentResponse = {},
            state = LoadingState.Ready(error = LoadingState.Error.None),
        )
    }
}

@Preview("LandingCard - Lite Selected")
@Composable
private fun LandingCardLitePreview() {
    ChessGymTheme {
        LandingCard(
            onDownload = {},
            onTierSelected = {},
            onDownloadConfirmation = {},
            onPermissionResponse = {},
            onCrashConsentResponse = {},
            state = LoadingState.Ready(selectedTier = DatabaseTier.Lite),
        )
    }
}

@Preview("LandingCard - With Error")
@Preview("LandingCard - With Error (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun LandingCardWithErrorPreview() {
    ChessGymTheme {
        LandingCard(
            onDownload = {},
            onTierSelected = {},
            onDownloadConfirmation = {},
            onPermissionResponse = {},
            onCrashConsentResponse = {},
            state = LoadingState.Ready(error = LoadingState.Error.NoInternet),
        )
    }
}
