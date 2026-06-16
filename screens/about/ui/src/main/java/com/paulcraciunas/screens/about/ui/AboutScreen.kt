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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.domain.api.billing.BillingUseCase
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.about.vm.AboutScreenInteractor
import com.paulcraciunas.screens.about.vm.AboutSection
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.design.components.ChessGymColumnCard
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.HairlineDivider
import com.paulcraciunas.screens.common.design.components.SectionHeader
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onSectionClicked: (AboutSection) -> Unit,
    interactions: AboutScreenInteractor,
    showDonationDialog: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { ChildAppBar(onBack = onNavigateBack, title = stringResource(R.string.about_title)) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        AboutContent(
            onSectionClicked = onSectionClicked,
            interactions = interactions,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        )

        if (showDonationDialog) {
            DonationDialog(
                onDismiss = interactions::onDismissDonationDialog,
                onAmountSelected = interactions::onDonateAmountSelected,
            )
        }
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
            .background(Design.colors.primarySoft)
            .verticalScroll(rememberScrollState())
            .padding(Design.dimensions.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
    ) {
        SectionHeader(title = stringResource(R.string.about_general_title))
        ChessGymColumnCard {
            NavigationRow(
                icon = Icons.Outlined.Person,
                title = stringResource(R.string.about_app_title),
                onClick = { onSectionClicked(AboutSection.App) },
            )
            HairlineDivider(modifier = Modifier.fillMaxWidth())
            NavigationRow(
                icon = Icons.Outlined.Edit,
                title = stringResource(R.string.about_feedback_title),
                onClick = { onSectionClicked(AboutSection.Feedback) },
            )
        }
        SectionHeader(title = stringResource(R.string.about_support_title))
        ChessGymColumnCard {
            ActionRow(
                icon = Icons.Outlined.Favorite,
                iconTint = Design.colors.donate,
                title = stringResource(R.string.about_donate_title),
                description = stringResource(R.string.about_donate_description),
                onClick = interactions::onDonateClicked,
            )
            HairlineDivider(modifier = Modifier.fillMaxWidth())
            ActionRow(
                icon = Icons.Outlined.Star,
                iconTint = Design.colors.rate,
                title = stringResource(R.string.about_rate_title),
                description = stringResource(R.string.about_rate_description),
                onClick = interactions::onRateAppClicked,
            )
        }
        SectionHeader(title = stringResource(R.string.about_legal_title))
        ChessGymColumnCard {
            NavigationRow(
                icon = ImageVector.vectorResource(id = R.drawable.icon_shield),
                title = stringResource(R.string.about_privacy_policy_title),
                onClick = { onSectionClicked(AboutSection.PrivacyPolicy) },
            )
            HairlineDivider(modifier = Modifier.fillMaxWidth())
            NavigationRow(
                icon = Icons.Outlined.CheckCircle,
                title = stringResource(R.string.about_terms_and_conditions_title),
                onClick = { onSectionClicked(AboutSection.TermsAndConditions) },
            )
            HairlineDivider(modifier = Modifier.fillMaxWidth())
            NavigationRow(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.about_terms_title),
                onClick = { onSectionClicked(AboutSection.TermsOfUse) },
            )
            HairlineDivider(modifier = Modifier.fillMaxWidth())
            NavigationRow(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.about_libraries_title),
                onClick = { onSectionClicked(AboutSection.Libraries) },
            )
        }
    }
}

@Composable
private fun NavigationRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = Design.dimensions.spacing.xxl,
                vertical = Design.dimensions.spacing.xl,
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Design.colors.inkSoft,
            modifier = Modifier.size(Design.dimensions.sizes.icon),
        )
        ChessGymSpacer(size = SpacerSize.LARGE)
        Text(
            text = title,
            style = Design.textStyles.title,
            color = Design.colors.ink,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Design.colors.inkMuted,
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Design.colors.inkSoft,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = Design.dimensions.spacing.xxl,
                vertical = Design.dimensions.spacing.xl,
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(Design.dimensions.sizes.icon),
        )
        ChessGymSpacer(size = SpacerSize.LARGE)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Design.textStyles.title,
                color = Design.colors.ink,
            )
            ChessGymSpacer(size = SpacerSize.SMALL)
            Text(
                text = description,
                style = Design.typography.bodySmall,
                color = Design.colors.inkSoft,
            )
        }
    }
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
            showDonationDialog = false,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

private object PreviewInteractions : AboutScreenInteractor {
    override fun onDonateClicked() {}
    override fun onDonateAmountSelected(product: BillingUseCase.DonationType) {}
    override fun onDismissDonationDialog() {}
    override fun onRateAppClicked() {}
}
