package com.paulcraciunas.screens.loading.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.Footer
import com.paulcraciunas.screens.common.controls.Header
import com.paulcraciunas.screens.common.controls.HeaderAlign
import com.paulcraciunas.screens.common.controls.HeaderSize
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.components.IconBadge
import com.paulcraciunas.screens.common.design.components.IconBorderType
import com.paulcraciunas.screens.common.design.components.IconSize
import com.paulcraciunas.screens.common.design.components.IconStyle
import com.paulcraciunas.screens.common.design.components.IconTintType
import com.paulcraciunas.screens.common.design.components.OutlineSegmentButton
import com.paulcraciunas.screens.common.design.components.PrimaryPillButton
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.annotatedTextResource
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
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
            .background(Design.colors.bg)
            .padding(horizontal = Design.dimensions.spacing.section),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            ChessGymSpacer(size = SpacerSize.SECTION)
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
            if (state.error != LoadingState.Error.None) {
                ErrorCard(error = state.error)
                ChessGymSpacer(size = SpacerSize.LARGE)
            }
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
            ChessGymSpacer(size = SpacerSize.SECTION)
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
private fun FeatureList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
    ) {
        FeatureItem(text = stringResource(R.string.landing_feature_puzzles))
        FeatureItem(text = stringResource(R.string.landing_feature_offline))
        FeatureItem(text = stringResource(R.string.landing_feature_rating))
    }
}

@Composable
private fun FeatureItem(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
    ) {
        IconBadge(
            imageVector = Icons.Default.Check,
            style = IconStyle.Circle,
            borderType = IconBorderType.None,
            tint = IconTintType.Accent,
            size = IconSize.Small
        )
        Text(
            text = text,
            style = Design.typography.bodyLarge,
            color = Design.colors.inkSoft,
        )
    }
}

@Composable
private fun TierSelector(
    selectedTier: DatabaseTier,
    onTierSelected: (DatabaseTier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.md),
    ) {
        Eyebrow(text = stringResource(R.string.landing_tier_label))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        ) {
            DatabaseTier.entries.forEach { tier ->
                OutlineSegmentButton(
                    text = tier.label,
                    selected = tier == selectedTier,
                    onClick = { onTierSelected(tier) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Text(
            text = selectedTier.description,
            style = Design.typography.bodySmall,
            color = Design.colors.inkMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorCard(
    error: LoadingState.Error,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Design.colors.danger.copy(alpha = 0.1f), Design.shapes.buttonOutline)
            .padding(Design.dimensions.spacing.xxl),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
            modifier = Modifier.fillMaxWidth(),
        ) {
            error.iconRes()?.let { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = Design.colors.danger,
                    modifier = Modifier.size(Design.dimensions.sizes.icon),
                )
            }
            Text(
                text = stringResource(error.res()),
                style = Design.typography.bodyMedium,
                color = Design.colors.danger,
                textAlign = TextAlign.Center,
            )
        }
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

@StringRes
private fun LoadingState.Error.res(): Int = when (this) {
    LoadingState.Error.DownloadFailed -> R.string.loading_error_download_failed
    LoadingState.Error.DecompressionFailed -> R.string.loading_error_decompression_failed
    LoadingState.Error.DatabaseWriteFailed -> R.string.loading_error_database_write_failed
    LoadingState.Error.NoPermission -> R.string.loading_error_permission
    LoadingState.Error.ConsentRequired -> R.string.loading_error_consent_required
    LoadingState.Error.NoInternet -> R.string.loading_error_no_internet
    LoadingState.Error.NotEnoughDiskSpace -> R.string.loading_error_not_enough_disk_space
    else -> R.string.loading_error_generic
}

@DrawableRes
private fun LoadingState.Error.iconRes(): Int? = when (this) {
    LoadingState.Error.NoInternet -> R.drawable.icon_no_network
    LoadingState.Error.NotEnoughDiskSpace -> R.drawable.icon_no_disk_space
    else -> null
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
