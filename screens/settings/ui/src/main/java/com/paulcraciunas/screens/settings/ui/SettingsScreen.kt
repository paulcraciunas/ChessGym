package com.paulcraciunas.screens.settings.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.design.components.ChessGymCard
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.HairlineDivider
import com.paulcraciunas.screens.common.design.components.OutlineSegmentButton
import com.paulcraciunas.screens.common.design.components.SectionHeader
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.ToggleRow
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.settings.vm.SettingsScreenInteractor
import com.paulcraciunas.screens.settings.vm.SettingsUiState
import com.paulcraciunas.settings.application.api.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    buildVersion: String,
    onNavigateBack: () -> Unit,
    interactions: SettingsScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.settings_title),
                navButton = { Back(onClick = onNavigateBack) },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingContent(Modifier.padding(innerPadding))
            else -> SettingsContent(
                uiState = uiState,
                buildVersion = buildVersion,
                interactions = interactions,
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    buildVersion: String,
    interactions: SettingsScreenInteractor,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .background(Design.colors.primarySoft)
            .padding(horizontal = Design.dimensions.spacing.xxl),
        contentPadding = contentPadding,
    ) {
        item {
            GeneralSection(uiState = uiState, interactions = interactions)
        }
        item {
            AppearanceSection(uiState = uiState, interactions = interactions)
        }
        item {
            PrivacySection(uiState = uiState, interactions = interactions)
        }
        item {
            BuildVersionFooter(buildVersion = buildVersion)
        }
    }
}

@Composable
private fun GeneralSection(
    uiState: SettingsUiState,
    interactions: SettingsScreenInteractor,
) {
    ChessGymSpacer(size = SpacerSize.DEFAULT)
    SectionHeader(title = stringResource(R.string.settings_section_general))
    ChessGymCard(contentPadding = PaddingValues(0.dp)) {
        Column {
            ToggleRow(
                title = stringResource(R.string.settings_haptic_feedback),
                subtitle = stringResource(R.string.settings_haptic_feedback_description),
                on = uiState.isHapticFeedbackEnabled,
                onChange = interactions::onHapticFeedbackToggled,
            )
            ToggleRow(
                title = stringResource(R.string.settings_auto_promote),
                subtitle = stringResource(R.string.settings_auto_promote_description),
                on = uiState.isAutoPromoteEnabled,
                onChange = interactions::onAutoPromoteToggled,
            )
            ToggleRow(
                title = stringResource(R.string.settings_auto_next_puzzle),
                subtitle = stringResource(R.string.settings_auto_next_puzzle_description),
                on = uiState.isAutoNextPuzzleEnabled,
                onChange = interactions::onAutoNextPuzzleToggled,
            )
            ToggleRow(
                title = stringResource(R.string.settings_show_borders),
                subtitle = stringResource(R.string.settings_show_borders_description),
                on = uiState.isShowBordersEnabled,
                onChange = interactions::onShowBordersToggled,
            )
            ToggleRow(
                title = stringResource(R.string.settings_highlight_legal_moves),
                subtitle = stringResource(R.string.settings_highlight_legal_moves_description),
                on = uiState.isHighlightLegalMovesEnabled,
                onChange = interactions::onHighlightLegalMovesToggled,
                last = true,
            )
        }
    }
}

@Composable
private fun AppearanceSection(
    uiState: SettingsUiState,
    interactions: SettingsScreenInteractor,
) {
    ChessGymSpacer(size = SpacerSize.DEFAULT)
    SectionHeader(title = stringResource(R.string.settings_section_ui))
    ChessGymCard(contentPadding = PaddingValues(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Design.dimensions.spacing.xxl,
                    vertical = Design.dimensions.spacing.xl,
                ),
        ) {
            Text(
                text = stringResource(R.string.settings_light_mode),
                style = Design.typography.titleSmall,
                color = Design.colors.ink,
            )
            ChessGymSpacer(size = SpacerSize.DEFAULT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
            ) {
                OutlineSegmentButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.settings_light_mode_light),
                    selected = uiState.lightMode == AppSettings.LightMode.Light,
                    onClick = { interactions.onLightModeSelected(AppSettings.LightMode.Light) },
                )
                OutlineSegmentButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.settings_light_mode_dark),
                    selected = uiState.lightMode == AppSettings.LightMode.Dark,
                    onClick = { interactions.onLightModeSelected(AppSettings.LightMode.Dark) },
                )
                OutlineSegmentButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.settings_light_mode_system),
                    selected = uiState.lightMode == AppSettings.LightMode.System,
                    onClick = { interactions.onLightModeSelected(AppSettings.LightMode.System) },
                )
            }
            ChessGymSpacer(size = SpacerSize.XXLARGE)
            HairlineDivider(modifier = Modifier.fillMaxWidth())
            ToggleRow(
                title = stringResource(R.string.settings_enable_animations),
                subtitle = stringResource(R.string.settings_enable_animations_description),
                on = uiState.isAnimationsEnabled,
                onChange = interactions::onAnimationsToggled,
                last = true,
            )
        }
    }
}

@Composable
private fun PrivacySection(
    uiState: SettingsUiState,
    interactions: SettingsScreenInteractor,
) {
    ChessGymSpacer(size = SpacerSize.DEFAULT)
    SectionHeader(title = stringResource(R.string.settings_section_privacy))
    ChessGymCard(contentPadding = PaddingValues(0.dp)) {
        ToggleRow(
            title = stringResource(R.string.settings_crash_reporting),
            subtitle = stringResource(R.string.settings_crash_reporting_description),
            on = uiState.isCrashReportingEnabled,
            onChange = interactions::onCrashReportingToggled,
            last = true,
        )
    }
}

@Composable
private fun BuildVersionFooter(
    buildVersion: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildVersion,
        textAlign = TextAlign.Center,
        style = Design.textStyles.footer,
        color = Design.colors.inkSubtle,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Design.dimensions.spacing.xxl),
    )
}

@Preview("Settings Screen")
@Preview("Settings Screen (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    ChessGymTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                isLoading = false,
            ),
            buildVersion = "Version 1.0-100",
            onNavigateBack = {},
            interactions = PreviewInteractions,
        )
    }
}

private object PreviewInteractions : SettingsScreenInteractor {
    override fun onHapticFeedbackToggled(isEnabled: Boolean) {}
    override fun onAutoPromoteToggled(isEnabled: Boolean) {}
    override fun onAutoNextPuzzleToggled(isEnabled: Boolean) {}
    override fun onShowBordersToggled(isEnabled: Boolean) {}
    override fun onHighlightLegalMovesToggled(isEnabled: Boolean) {}
    override fun onLightModeSelected(mode: AppSettings.LightMode) {}
    override fun onAnimationsToggled(isEnabled: Boolean) {}
    override fun onCrashReportingToggled(isEnabled: Boolean) {}
}
