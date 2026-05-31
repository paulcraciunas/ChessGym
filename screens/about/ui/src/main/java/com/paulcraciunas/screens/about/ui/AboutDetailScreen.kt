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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.about.vm.AboutSection
import com.paulcraciunas.screens.about.vm.LibraryInfo
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.Footer
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDetailScreen(
    section: AboutSection,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    libraries: List<LibraryInfo> = emptyList(),
    onEmailClicked: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = { ChildAppBar(onBack = onNavigateBack, title = resolveTitle(section)) },
        modifier = modifier,
    ) { innerPadding ->
        DetailContent(
            section = section,
            libraries = libraries,
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
    onEmailClicked: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = Design.dimensions.spacing.xgut,
                vertical = Design.dimensions.spacing.xxl,
            ),
    ) {
        when (section) {
            AboutSection.CREATOR -> CreatorContent()
            AboutSection.CONTACT -> ContactContent(onEmailClicked = onEmailClicked)
            AboutSection.FEEDBACK -> FeedbackContent(onEmailClicked = onEmailClicked)
            AboutSection.LIBRARIES -> LibrariesContent(libraries = libraries)
            AboutSection.PRIVACY_POLICY -> PrivacyPolicyContent()
            AboutSection.TERMS_OF_USE -> TermsOfUseContent()
            AboutSection.TERMS_AND_CONDITIONS -> TermsAndConditionsContent()
        }
        ChessGymSpacer(size = SpacerSize.SECTION)
        Footer()
        ChessGymSpacer(size = SpacerSize.HUGE)
    }
}

@Composable
private fun CreatorContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        DetailDescription(text = stringResource(R.string.about_creator_description))
    }
}

@Composable
private fun ContactContent(
    onEmailClicked: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        DetailDescription(text = stringResource(R.string.about_contact_description))
        ChessGymSpacer(size = SpacerSize.XXLARGE)
        onEmailClicked?.let {
            EmailRow(
                email = stringResource(R.string.about_contact_email),
                onClick = it,
            )
        }
    }
}

@Composable
private fun FeedbackContent(
    onEmailClicked: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        DetailDescription(text = stringResource(R.string.about_feedback_description))
        ChessGymSpacer(size = SpacerSize.XXLARGE)
        onEmailClicked?.let {
            EmailRow(
                email = stringResource(R.string.about_feedback_email),
                onClick = it,
            )
        }
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
        DetailDescription(text = stringResource(R.string.about_privacy_policy_content))
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
private fun EmailRow(
    email: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = Design.dimensions.spacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            tint = Design.colors.primary,
            modifier = Modifier.size(Design.dimensions.sizes.icon),
        )
        Text(
            text = email,
            style = Design.typography.bodyMedium,
            color = Design.colors.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.padding(start = Design.dimensions.spacing.sm),
        )
    }
}

@Composable
private fun LibraryRow(
    library: LibraryInfo,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Design.dimensions.spacing.s),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = library.name,
                style = Design.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (library.url.isNotBlank()) {
                Text(
                    text = library.url,
                    style = Design.typography.bodySmall,
                    color = Design.colors.primary,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
        Text(
            text = library.license,
            style = Design.typography.bodySmall,
            color = Design.colors.inkSoft,
        )
    }
}

@Composable
private fun resolveTitle(section: AboutSection): String = when (section) {
    AboutSection.CREATOR -> stringResource(R.string.about_creator_title)
    AboutSection.CONTACT -> stringResource(R.string.about_contact_title)
    AboutSection.FEEDBACK -> stringResource(R.string.about_feedback_title)
    AboutSection.LIBRARIES -> stringResource(R.string.about_libraries_title)
    AboutSection.PRIVACY_POLICY -> stringResource(R.string.about_privacy_policy_title)
    AboutSection.TERMS_OF_USE -> stringResource(R.string.about_terms_title)
    AboutSection.TERMS_AND_CONDITIONS -> stringResource(R.string.about_terms_and_conditions_title)
}

@Preview("Detail - Creator")
@Preview("Detail - Creator (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CreatorDetailPreview() {
    ChessGymTheme {
        AboutDetailScreen(
            section = AboutSection.CREATOR,
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
            section = AboutSection.LIBRARIES,
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
            section = AboutSection.TERMS_AND_CONDITIONS,
            onNavigateBack = {},
        )
    }
}
