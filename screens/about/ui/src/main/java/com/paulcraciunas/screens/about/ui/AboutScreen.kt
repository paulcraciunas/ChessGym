package com.paulcraciunas.screens.about.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.about.vm.AboutScreenInteractor
import com.paulcraciunas.screens.about.vm.AboutSection
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onSectionClicked: (AboutSection) -> Unit,
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
            onSectionClicked = onSectionClicked,
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
    onSectionClicked: (AboutSection) -> Unit,
    interactions: AboutScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SectionGroup(title = stringResource(R.string.about_general_title)) {
            NavigationItem(
                icon = Icons.Outlined.Person,
                title = stringResource(R.string.about_creator_title),
                onClick = { onSectionClicked(AboutSection.CREATOR) },
            )
            GroupDivider()
            NavigationItem(
                icon = Icons.Outlined.Email,
                title = stringResource(R.string.about_contact_title),
                onClick = { onSectionClicked(AboutSection.CONTACT) },
            )
            GroupDivider()
            NavigationItem(
                icon = Icons.Outlined.Edit,
                title = stringResource(R.string.about_feedback_title),
                onClick = { onSectionClicked(AboutSection.FEEDBACK) },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        SectionGroup(title = stringResource(R.string.about_support_title)) {
            ActionItem(
                icon = Icons.Outlined.Favorite,
                title = stringResource(R.string.about_donate_title),
                description = stringResource(R.string.about_donate_description),
                onClick = interactions::onDonateClicked,
            )
            GroupDivider()
            ActionItem(
                icon = Icons.Outlined.Star,
                title = stringResource(R.string.about_rate_title),
                description = stringResource(R.string.about_rate_description),
                onClick = interactions::onRateAppClicked,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        SectionGroup(title = stringResource(R.string.about_legal_title)) {
            NavigationItem(
                icon = Icons.Outlined.CheckCircle,
                title = stringResource(R.string.about_terms_and_conditions_title),
                onClick = { onSectionClicked(AboutSection.TERMS_AND_CONDITIONS) },
            )
            GroupDivider()
            NavigationItem(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.about_terms_title),
                onClick = { onSectionClicked(AboutSection.TERMS_OF_USE) },
            )
            GroupDivider()
            NavigationItem(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.about_libraries_title),
                onClick = { onSectionClicked(AboutSection.LIBRARIES) },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            content()
        }
    }
}

@Composable
private fun NavigationItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
    )
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
    )
}

@Composable
private fun GroupDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Preview("About Screen")
@Preview("About Screen (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AboutScreenPreview() {
    ChessGymTheme {
        AboutScreen(
            onNavigateBack = {},
            onSectionClicked = {},
            interactions = PreviewInteractions,
        )
    }
}

private object PreviewInteractions : AboutScreenInteractor {
    override fun onDonateClicked() {}
    override fun onRateAppClicked() {}
}
