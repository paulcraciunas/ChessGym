package com.paulcraciunas.screens.settings.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.design.components.ChessGymColumnCard
import com.paulcraciunas.screens.common.design.components.HairlineDivider
import com.paulcraciunas.screens.common.design.components.OutlineSegmentButton
import com.paulcraciunas.screens.common.design.components.SectionHeader
import com.paulcraciunas.screens.common.design.components.ToggleRow
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.settings.vm.SettingsUiState
import com.paulcraciunas.settings.application.api.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    buildVersion: String,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSoundToggled: (Boolean) -> Unit = {},
    onHapticFeedbackToggled: (Boolean) -> Unit = {},
    onAutoPromoteToggled: (Boolean) -> Unit = {},
    onAutoNextPuzzleToggled: (Boolean) -> Unit = {},
    onShowBordersToggled: (Boolean) -> Unit = {},
    onHighlightLegalMovesToggled: (Boolean) -> Unit = {},
    onLightModeSelected: (AppSettings.LightMode) -> Unit = {},
    onLanguageSelected: (SettingsUiState.AppLanguage) -> Unit = {},
    onAnimationsToggled: (Boolean) -> Unit = {},
    onCrashReportingToggled: (Boolean) -> Unit = {},
) {
    Scaffold(
        topBar = { ChildAppBar(title = stringResource(R.string.settings_title), onBack = onNavigateBack) },
        modifier = modifier,
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingContent(Modifier.padding(innerPadding))
            else -> SettingsContent(
                uiState = uiState,
                buildVersion = buildVersion,
                contentPadding = PaddingValues(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding()),
                modifier = Modifier.fillMaxSize(),
                onSoundToggled = onSoundToggled,
                onHapticFeedbackToggled = onHapticFeedbackToggled,
                onAutoPromoteToggled = onAutoPromoteToggled,
                onAutoNextPuzzleToggled = onAutoNextPuzzleToggled,
                onShowBordersToggled = onShowBordersToggled,
                onHighlightLegalMovesToggled = onHighlightLegalMovesToggled,
                onLightModeSelected = onLightModeSelected,
                onLanguageSelected = onLanguageSelected,
                onAnimationsToggled = onAnimationsToggled,
                onCrashReportingToggled = onCrashReportingToggled,
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    buildVersion: String,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onSoundToggled: (Boolean) -> Unit = {},
    onHapticFeedbackToggled: (Boolean) -> Unit = {},
    onAutoPromoteToggled: (Boolean) -> Unit = {},
    onAutoNextPuzzleToggled: (Boolean) -> Unit = {},
    onShowBordersToggled: (Boolean) -> Unit = {},
    onHighlightLegalMovesToggled: (Boolean) -> Unit = {},
    onLightModeSelected: (AppSettings.LightMode) -> Unit = {},
    onLanguageSelected: (SettingsUiState.AppLanguage) -> Unit = {},
    onAnimationsToggled: (Boolean) -> Unit = {},
    onCrashReportingToggled: (Boolean) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .background(Design.colors.primarySoft)
            .padding(
                horizontal = Design.dimensions.spacing.xxl,
                vertical = Design.dimensions.spacing.sm,
            ),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
    ) {
        item {
            GeneralSection(
                uiState = uiState,
                onSoundToggled = onSoundToggled,
                onHapticFeedbackToggled = onHapticFeedbackToggled,
                onAutoPromoteToggled = onAutoPromoteToggled,
                onAutoNextPuzzleToggled = onAutoNextPuzzleToggled,
                onShowBordersToggled = onShowBordersToggled,
                onHighlightLegalMovesToggled = onHighlightLegalMovesToggled,
            )
        }
        item {
            AppearanceSection(
                uiState = uiState,
                onLightModeSelected = onLightModeSelected,
                onLanguageSelected = onLanguageSelected,
                onAnimationsToggled = onAnimationsToggled,
            )
        }
        item {
            PrivacySection(uiState = uiState, onCrashReportingToggled = onCrashReportingToggled)
        }
        item {
            VersionFooter(buildVersion = buildVersion)
        }
    }
}

@Composable
private fun GeneralSection(
    uiState: SettingsUiState,
    onSoundToggled: (Boolean) -> Unit = {},
    onHapticFeedbackToggled: (Boolean) -> Unit = {},
    onAutoPromoteToggled: (Boolean) -> Unit = {},
    onAutoNextPuzzleToggled: (Boolean) -> Unit = {},
    onShowBordersToggled: (Boolean) -> Unit = {},
    onHighlightLegalMovesToggled: (Boolean) -> Unit = {},
) {
    SectionHeader(title = stringResource(R.string.settings_section_general))
    ChessGymColumnCard {
        ToggleRow(
            title = stringResource(R.string.settings_sound),
            subtitle = stringResource(R.string.settings_sound_description),
            on = uiState.isSoundEnabled,
            onChange = onSoundToggled,
        )
        ToggleRow(
            title = stringResource(R.string.settings_haptic_feedback),
            subtitle = stringResource(R.string.settings_haptic_feedback_description),
            on = uiState.isHapticFeedbackEnabled,
            onChange = onHapticFeedbackToggled,
        )
        ToggleRow(
            title = stringResource(R.string.settings_auto_promote),
            subtitle = stringResource(R.string.settings_auto_promote_description),
            on = uiState.isAutoPromoteEnabled,
            onChange = onAutoPromoteToggled,
        )
        ToggleRow(
            title = stringResource(R.string.settings_auto_next_puzzle),
            subtitle = stringResource(R.string.settings_auto_next_puzzle_description),
            on = uiState.isAutoNextPuzzleEnabled,
            onChange = onAutoNextPuzzleToggled,
        )
        ToggleRow(
            title = stringResource(R.string.settings_show_borders),
            subtitle = stringResource(R.string.settings_show_borders_description),
            on = uiState.isShowBordersEnabled,
            onChange = onShowBordersToggled,
        )
        ToggleRow(
            title = stringResource(R.string.settings_highlight_legal_moves),
            subtitle = stringResource(R.string.settings_highlight_legal_moves_description),
            on = uiState.isHighlightLegalMovesEnabled,
            onChange = onHighlightLegalMovesToggled,
            last = true,
        )
    }
}

@Composable
private fun AppearanceSection(
    uiState: SettingsUiState,
    onLightModeSelected: (AppSettings.LightMode) -> Unit = {},
    onLanguageSelected: (SettingsUiState.AppLanguage) -> Unit = {},
    onAnimationsToggled: (Boolean) -> Unit = {},
) {
    SectionHeader(title = stringResource(R.string.settings_section_ui))
    ChessGymColumnCard(
        contentPadding = PaddingValues(
            horizontal = Design.dimensions.spacing.xxl,
            vertical = Design.dimensions.spacing.xl,
        ),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.settings_light_mode),
            style = Design.typography.titleSmall,
            color = Design.colors.ink,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        ) {
            OutlineSegmentButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.settings_light_mode_light),
                selected = uiState.lightMode == AppSettings.LightMode.Light,
                onClick = { onLightModeSelected(AppSettings.LightMode.Light) },
            )
            OutlineSegmentButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.settings_light_mode_dark),
                selected = uiState.lightMode == AppSettings.LightMode.Dark,
                onClick = { onLightModeSelected(AppSettings.LightMode.Dark) },
            )
            OutlineSegmentButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.settings_light_mode_system),
                selected = uiState.lightMode == AppSettings.LightMode.System,
                onClick = { onLightModeSelected(AppSettings.LightMode.System) },
            )
        }
        HairlineDivider(modifier = Modifier.fillMaxWidth())
        LanguagePicker(
            selectedLanguage = uiState.language,
            onLanguageSelected = onLanguageSelected,
        )
        HairlineDivider(modifier = Modifier.fillMaxWidth())
        ToggleRow(
            title = stringResource(R.string.settings_enable_animations),
            subtitle = stringResource(R.string.settings_enable_animations_description),
            on = uiState.isAnimationsEnabled,
            onChange = onAnimationsToggled,
            last = true,
            contentPadding = PaddingValues.Zero,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePicker(
    selectedLanguage: SettingsUiState.AppLanguage,
    onLanguageSelected: (SettingsUiState.AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLanguage.fullName(),
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = stringResource(R.string.settings_language),
                    style = Design.typography.titleSmall,
                    color = Design.colors.ink,
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            SettingsUiState.AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = selectedLanguage.fullName(),
                            style = Design.typography.bodyMedium,
                            color = if (language == selectedLanguage) Design.colors.primary else Design.colors.ink,
                        )
                    },
                    onClick = {
                        onLanguageSelected(language)
                        isExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun PrivacySection(
    uiState: SettingsUiState,
    onCrashReportingToggled: (Boolean) -> Unit = {},
) {
    SectionHeader(title = stringResource(R.string.settings_section_privacy))
    ChessGymColumnCard {
        ToggleRow(
            title = stringResource(R.string.settings_crash_reporting),
            subtitle = stringResource(R.string.settings_crash_reporting_description),
            on = uiState.isCrashReportingEnabled,
            onChange = onCrashReportingToggled,
            last = true,
        )
    }
}

@Composable
private fun SettingsUiState.AppLanguage.fullName(): String = "${flag()} ${displayName()}"

private fun SettingsUiState.AppLanguage.flag(): String = when (this) {
    SettingsUiState.AppLanguage.System -> "🌐"
    SettingsUiState.AppLanguage.English -> "🇬🇧"
    SettingsUiState.AppLanguage.German -> "🇩🇪"
    SettingsUiState.AppLanguage.Spanish -> "🇪🇸"
    SettingsUiState.AppLanguage.French -> "🇫🇷"
    SettingsUiState.AppLanguage.Hindi -> "🇮🇳"
    SettingsUiState.AppLanguage.Indonesian -> "🇮🇩"
    SettingsUiState.AppLanguage.Japanese -> "🇯🇵"
    SettingsUiState.AppLanguage.Korean -> "🇰🇷"
    SettingsUiState.AppLanguage.BrazilianPortuguese -> "🇧🇷"
    SettingsUiState.AppLanguage.Russian -> "🇷🇺"
    SettingsUiState.AppLanguage.SimplifiedChinese -> "🇨🇳"
}

@Composable
private fun SettingsUiState.AppLanguage.displayName(): String = when (this) {
    SettingsUiState.AppLanguage.System -> stringResource(R.string.settings_light_mode_system)
    SettingsUiState.AppLanguage.English -> "English"
    SettingsUiState.AppLanguage.German -> "Deutsch"
    SettingsUiState.AppLanguage.Spanish -> "Español"
    SettingsUiState.AppLanguage.French -> "Français"
    SettingsUiState.AppLanguage.Hindi -> "हिन्दी"
    SettingsUiState.AppLanguage.Indonesian -> "Bahasa Indonesia"
    SettingsUiState.AppLanguage.Japanese -> "日本語"
    SettingsUiState.AppLanguage.Korean -> "한국어"
    SettingsUiState.AppLanguage.BrazilianPortuguese -> "Português (Brasil)"
    SettingsUiState.AppLanguage.Russian -> "Русский"
    SettingsUiState.AppLanguage.SimplifiedChinese -> "中文(简体)"
}

@Composable
private fun VersionFooter(
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
                language = SettingsUiState.AppLanguage.English,
            ),
            buildVersion = "Version 1.0-100",
        )
    }
}
