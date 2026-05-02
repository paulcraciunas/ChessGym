package com.paulcraciunas.screens.settings.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.LoadingContent
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    buildVersion: String,
    interactions: SettingsScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        GeneralSection(uiState = uiState, interactions = interactions)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        AppearanceSection(uiState = uiState, interactions = interactions)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        PrivacySection(uiState = uiState, interactions = interactions)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Spacer(modifier = Modifier.weight(1f))
        BuildVersionFooter(buildVersion = buildVersion)
    }
}

@Composable
private fun GeneralSection(
    uiState: SettingsUiState,
    interactions: SettingsScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.settings_section_general))
        SettingsSwitch(
            title = stringResource(R.string.settings_haptic_feedback),
            description = stringResource(R.string.settings_haptic_feedback_description),
            isChecked = uiState.isHapticFeedbackEnabled,
            onCheckedChange = interactions::onHapticFeedbackToggled,
        )
        SettingsSwitch(
            title = stringResource(R.string.settings_auto_promote),
            description = stringResource(R.string.settings_auto_promote_description),
            isChecked = uiState.isAutoPromoteEnabled,
            onCheckedChange = interactions::onAutoPromoteToggled,
        )
        SettingsSwitch(
            title = stringResource(R.string.settings_show_borders),
            description = stringResource(R.string.settings_show_borders_description),
            isChecked = uiState.isShowBordersEnabled,
            onCheckedChange = interactions::onShowBordersToggled,
        )
        SettingsSwitch(
            title = stringResource(R.string.settings_highlight_legal_moves),
            description = stringResource(R.string.settings_highlight_legal_moves_description),
            isChecked = uiState.isHighlightLegalMovesEnabled,
            onCheckedChange = interactions::onHighlightLegalMovesToggled,
        )
    }
}

@Composable
private fun AppearanceSection(
    uiState: SettingsUiState,
    interactions: SettingsScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.settings_section_ui))
        LightModeSelector(
            selectedMode = uiState.lightMode,
            onModeSelected = interactions::onLightModeSelected,
        )
        SettingsSwitch(
            title = stringResource(R.string.settings_enable_animations),
            description = stringResource(R.string.settings_enable_animations_description),
            isChecked = uiState.isAnimationsEnabled,
            onCheckedChange = interactions::onAnimationsToggled,
        )
    }
}

@Composable
private fun PrivacySection(
    uiState: SettingsUiState,
    interactions: SettingsScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.settings_section_privacy))
        SettingsSwitch(
            title = stringResource(R.string.settings_crash_reporting),
            description = stringResource(R.string.settings_crash_reporting_description),
            isChecked = uiState.isCrashReportingEnabled,
            onCheckedChange = interactions::onCrashReportingToggled,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 12.dp),
    )
}

@Composable
private fun SettingsSwitch(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun LightModeSelector(
    selectedMode: AppSettings.LightMode,
    onModeSelected: (AppSettings.LightMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.settings_light_mode),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column(modifier = Modifier.selectableGroup()) {
            LightModeOption(
                label = stringResource(R.string.settings_light_mode_light),
                isSelected = selectedMode == AppSettings.LightMode.Light,
                onClick = { onModeSelected(AppSettings.LightMode.Light) },
            )
            LightModeOption(
                label = stringResource(R.string.settings_light_mode_dark),
                isSelected = selectedMode == AppSettings.LightMode.Dark,
                onClick = { onModeSelected(AppSettings.LightMode.Dark) },
            )
            LightModeOption(
                label = stringResource(R.string.settings_light_mode_system),
                isSelected = selectedMode == AppSettings.LightMode.System,
                onClick = { onModeSelected(AppSettings.LightMode.System) },
            )
        }
    }
}

@Composable
private fun LightModeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
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
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
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
    override fun onShowBordersToggled(isEnabled: Boolean) {}
    override fun onHighlightLegalMovesToggled(isEnabled: Boolean) {}
    override fun onLightModeSelected(mode: AppSettings.LightMode) {}
    override fun onAnimationsToggled(isEnabled: Boolean) {}
    override fun onCrashReportingToggled(isEnabled: Boolean) {}
}
