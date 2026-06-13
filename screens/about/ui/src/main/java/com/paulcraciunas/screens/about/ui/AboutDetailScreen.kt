package com.paulcraciunas.screens.about.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.about.vm.AboutSection
import com.paulcraciunas.screens.about.vm.LibraryInfo
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.Footer
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.annotatedTextResource
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDetailScreen(
    section: AboutSection,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String = "1.0",
    libraries: List<LibraryInfo> = emptyList(),
    onEmailClicked: (String) -> Unit = {},
) {
    Scaffold(
        topBar = { ChildAppBar(onBack = onNavigateBack, title = resolveTitle(section)) },
        modifier = modifier,
    ) { innerPadding ->
        DetailContent(
            section = section,
            libraries = libraries,
            appVersion = appVersion,
            onEmailClicked = onEmailClicked,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(Design.colors.primarySoft),
        )
    }
}

@Composable
private fun DetailContent(
    section: AboutSection,
    libraries: List<LibraryInfo>,
    appVersion: String,
    modifier: Modifier = Modifier,
    onEmailClicked: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = Design.dimensions.spacing.xgut,
                vertical = Design.dimensions.spacing.xxl,
            ),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        when (section) {
            AboutSection.App -> AppContent(onEmailClicked = onEmailClicked, appVersion = appVersion)
            AboutSection.Feedback -> FeedbackContent(onEmailClicked = onEmailClicked)
            AboutSection.Libraries -> LibrariesContent(libraries = libraries)
            AboutSection.PrivacyPolicy -> PrivacyPolicyContent()
            AboutSection.TermsOfUse -> TermsOfUseContent()
            AboutSection.TermsAndConditions -> TermsAndConditionsContent()
        }
        Footer()
    }
}

@Composable
private fun AppContent(
    modifier: Modifier = Modifier,
    appVersion: String = "1.0",
    onEmailClicked: (String) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs)
    ) {
        val appNameAndVersion = stringResource(R.string.app_name) + " V${appVersion}"
        Text(appNameAndVersion, style = MaterialTheme.typography.titleLarge)
        Text(
            text = appDescriptionString(),
            style = Design.typography.bodyMedium.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
        EmailRow(
            email = stringResource(R.string.about_contact_email),
            onClick = { onEmailClicked("mailto:contact@chessgym.app") },
        )
        ChessGymSpacer(size = SpacerSize.SECTION)
        Text(
            text = annotatedTextResource(R.string.about_dedication_danya),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun appDescriptionString() = buildAnnotatedString {
    append(stringResource(R.string.about_app_description))
    withLink(
        LinkAnnotation.Url(
            url = "https://github.com/paulcraciunas",
            styles = TextLinkStyles(
                style = SpanStyle(
                    color = Design.colors.primary,
                    textDecoration = TextDecoration.Underline
                )
            )
        )
    ) {
        append("(GitHub)")
    }
}

@Composable
private fun FeedbackContent(
    onEmailClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        DetailDescription(text = stringResource(R.string.about_feedback_description))
        ChessGymSpacer(size = SpacerSize.XXLARGE)
        EmailRow(
            email = stringResource(R.string.about_feedback_email),
            onClick = { onEmailClicked("mailto:feedback@chessgym.app") },
        )
    }
}

@Composable
private fun LibrariesContent(
    libraries: List<LibraryInfo>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        DetailDescription(text = stringResource(R.string.about_libraries_description))
        ChessGymSpacer(size = SpacerSize.XXLARGE)
        libraries.forEach { library ->
            LibraryRow(library = library)
        }
    }
}

@Composable
private fun PrivacyPolicyContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        DetailDescription(text = annotatedTextResource(R.string.about_privacy_policy_content))
    }
}

@Composable
private fun TermsOfUseContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        DetailDescription(text = stringResource(R.string.about_terms_content))
    }
}

@Composable
private fun TermsAndConditionsContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        DetailDescription(text = stringResource(R.string.about_terms_and_conditions_content))
    }
}

@Composable
private fun DetailDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = Design.typography.bodyLarge,
        color = Design.colors.ink,
        modifier = modifier,
    )
}

@Composable
private fun DetailDescription(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = Design.typography.bodyLarge,
        color = Design.colors.ink,
        modifier = modifier,
    )
}

@Composable
private fun EmailRow(
    email: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onClick() },
    ) {
        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            tint = Design.colors.primary,
            modifier = Modifier
                .size(Design.dimensions.sizes.icon)
                .alignByBaseline(),
        )
        Text(
            text = email,
            style = Design.typography.bodyMedium,
            color = Design.colors.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .padding(start = Design.dimensions.spacing.sm)
                .alignByBaseline(),
        )
    }
}

@Composable
private fun LibraryRow(
    library: LibraryInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(vertical = Design.dimensions.spacing.s),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = library.name,
                style = Design.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = library.license,
                style = Design.typography.bodySmall,
                color = Design.colors.inkSoft,
            )
        }
        if (library.url.isNotBlank()) {
            Text(
                text = library.url,
                style = Design.typography.bodySmall,
                color = Design.colors.primary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
private fun resolveTitle(section: AboutSection): String = when (section) {
    AboutSection.App -> stringResource(R.string.about_app_title)
    AboutSection.Feedback -> stringResource(R.string.about_feedback_title)
    AboutSection.Libraries -> stringResource(R.string.about_libraries_title)
    AboutSection.PrivacyPolicy -> stringResource(R.string.about_privacy_policy_title)
    AboutSection.TermsOfUse -> stringResource(R.string.about_terms_title)
    AboutSection.TermsAndConditions -> stringResource(R.string.about_terms_and_conditions_title)
}

@Preview("Detail - Creator")
@Preview("Detail - Creator (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CreatorDetailPreview() {
    ChessGymTheme {
        AboutDetailScreen(
            section = AboutSection.App,
            onNavigateBack = {},
        )
    }
}

@Preview("Detail - Libraries")
@Preview("Detail - Libraries (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun LibrariesDetailPreview() {
    ChessGymTheme {
        AboutDetailScreen(
            section = AboutSection.Libraries,
            onNavigateBack = {},
            libraries = listOf(
                LibraryInfo("Kotlin", "https://kotlinlang.org", "Apache License 2.0"),
                LibraryInfo("Jetpack Compose", "https://developer.android.com", "Apache License 2.0"),
                LibraryInfo("Hilt", "https://dagger.dev/hilt", "Apache License 2.0"),
            ),
        )
    }
}

@Preview("Detail - Terms and Conditions")
@Composable
private fun TermsAndConditionsDetailPreview() {
    ChessGymTheme {
        AboutDetailScreen(
            section = AboutSection.TermsAndConditions,
            onNavigateBack = {},
        )
    }
}
