package com.paulcraciunas.screens.about.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.about.vm.AboutScreenInteractor
import com.paulcraciunas.screens.about.vm.AboutUiState
import com.paulcraciunas.screens.about.vm.LibraryInfo
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun AboutScreen(
    uiState: AboutUiState,
    onNavigateBack: () -> Unit,
    onContactEmail: () -> Unit,
    onFeedbackEmail: () -> Unit,
    interactions: AboutScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.about_title),
                navButton = { Back(onClick = onNavigateBack) },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        AboutContent(
            uiState = uiState,
            onContactEmail = onContactEmail,
            onFeedbackEmail = onFeedbackEmail,
            interactions = interactions,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        )
    }
}

@Composable
private fun AboutContent(
    uiState: AboutUiState,
    onContactEmail: () -> Unit,
    onFeedbackEmail: () -> Unit,
    interactions: AboutScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        CreatorSection()
        SectionDivider()
        ContactSection(onContactEmail = onContactEmail)
        SectionDivider()
        FeedbackSection(onFeedbackEmail = onFeedbackEmail)
        SectionDivider()
        SupportSection(interactions = interactions)
        SectionDivider()
        LibrariesSection(libraries = uiState.libraries)
        SectionDivider()
        TermsSection()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CreatorSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        SectionHeader(title = stringResource(R.string.about_creator_title))
        Text(
            text = stringResource(R.string.about_creator_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ContactSection(
    onContactEmail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        SectionHeader(title = stringResource(R.string.about_contact_title))
        Text(
            text = stringResource(R.string.about_contact_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        EmailRow(
            email = stringResource(R.string.about_contact_email),
            onClick = onContactEmail,
        )
    }
}

@Composable
private fun FeedbackSection(
    onFeedbackEmail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        SectionHeader(title = stringResource(R.string.about_feedback_title))
        Text(
            text = stringResource(R.string.about_feedback_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        EmailRow(
            email = stringResource(R.string.about_feedback_email),
            onClick = onFeedbackEmail,
        )
    }
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
            .padding(vertical = 4.dp),
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
private fun SupportSection(
    interactions: AboutScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        SectionHeader(title = stringResource(R.string.about_support_title))
        ActionRow(
            icon = { Icon(Icons.Outlined.Favorite, contentDescription = null) },
            title = stringResource(R.string.about_donate_title),
            description = stringResource(R.string.about_donate_description),
            onClick = interactions::onDonateClicked,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActionRow(
            icon = { Icon(Icons.Outlined.Star, contentDescription = null) },
            title = stringResource(R.string.about_rate_title),
            description = stringResource(R.string.about_rate_description),
            onClick = interactions::onRateAppClicked,
        )
    }
}

@Composable
private fun ActionRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            icon()
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LibrariesSection(
    libraries: List<LibraryInfo>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        SectionHeader(title = stringResource(R.string.about_libraries_title))
        Text(
            text = stringResource(R.string.about_libraries_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        libraries.forEach { library ->
            LibraryRow(library = library)
        }
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
            .padding(vertical = 4.dp),
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
private fun TermsSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        SectionHeader(title = stringResource(R.string.about_terms_title))
        Text(
            text = stringResource(R.string.about_terms_content),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        modifier = modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(vertical = 4.dp))
}

@Preview("About Screen")
@Preview("About Screen (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AboutScreenPreview() {
    ChessGymTheme {
        AboutScreen(
            uiState = AboutUiState(
                libraries = listOf(
                    LibraryInfo("Kotlin", "https://kotlinlang.org", "Apache License 2.0"),
                    LibraryInfo("Jetpack Compose", "https://developer.android.com", "Apache License 2.0"),
                    LibraryInfo("Hilt", "https://dagger.dev/hilt", "Apache License 2.0"),
                ),
            ),
            onNavigateBack = {},
            onContactEmail = {},
            onFeedbackEmail = {},
            interactions = PreviewInteractions,
        )
    }
}

private object PreviewInteractions : AboutScreenInteractor {
    override fun onDonateClicked() {}
    override fun onRateAppClicked() {}
}
