package com.paulcraciunas.screens.about.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.about.vm.AboutSection
import com.paulcraciunas.screens.about.vm.LibraryInfo
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun AboutDetailScreen(
    section: AboutSection,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    libraries: List<LibraryInfo> = emptyList(),
    onEmailClicked: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = resolveTitle(section),
                navButton = { Back(onClick = onNavigateBack) },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        DetailContent(
            section = section,
            libraries = libraries,
            onEmailClicked = onEmailClicked,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
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
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        when (section) {
            AboutSection.CREATOR -> CreatorContent()
            AboutSection.CONTACT -> ContactContent(onEmailClicked = onEmailClicked)
            AboutSection.FEEDBACK -> FeedbackContent(onEmailClicked = onEmailClicked)
            AboutSection.LIBRARIES -> LibrariesContent(libraries = libraries)
            AboutSection.TERMS_OF_USE -> TermsOfUseContent()
            AboutSection.TERMS_AND_CONDITIONS -> TermsAndConditionsContent()
        }
        Spacer(modifier = Modifier.height(48.dp))
        BrandingFooter()
        Spacer(modifier = Modifier.height(24.dp))
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
        Spacer(modifier = Modifier.height(16.dp))
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
        Spacer(modifier = Modifier.height(16.dp))
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
        Spacer(modifier = Modifier.height(16.dp))
        libraries.forEach { library ->
            LibraryRow(library = library)
        }
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
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
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
            .padding(vertical = 8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.padding(start = 8.dp),
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
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = library.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (library.url.isNotBlank()) {
                Text(
                    text = library.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
        Text(
            text = library.license,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BrandingFooter(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Image(
            painter = painterResource(R.drawable.knight_white),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun resolveTitle(section: AboutSection): String = when (section) {
    AboutSection.CREATOR -> stringResource(R.string.about_creator_title)
    AboutSection.CONTACT -> stringResource(R.string.about_contact_title)
    AboutSection.FEEDBACK -> stringResource(R.string.about_feedback_title)
    AboutSection.LIBRARIES -> stringResource(R.string.about_libraries_title)
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
