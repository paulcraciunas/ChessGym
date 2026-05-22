package com.paulcraciunas.screens.about.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.billing.BillingManager
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.DialogIconTone
import com.paulcraciunas.screens.common.design.components.HairlineDivider
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.annotatedTextResource
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
fun DonationDialog(
    onDismiss: () -> Unit,
    onAmountSelected: (BillingManager.DonationType) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = onDismiss,
        title = {
            IconTitle(
                title = stringResource(R.string.about_donate_dialog_title),
                icon = Icons.Default.Favorite,
                tone = DialogIconTone.Accent,
            )
        },
        buttons = {
            Outlined(
                text = stringResource(R.string.generic_not_now),
                onClick = onDismiss,
                wide = false,
            )
        },
        modifier = modifier,
    ) {
        Custom {
            Message(text = annotatedTextResource(R.string.about_donate_dialog_description))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ChessGymSpacer(size = SpacerSize.LARGE)
                DonationOption(
                    title = stringResource(R.string.about_donate_small),
                    onClick = { onAmountSelected(BillingManager.DonationType.Small) }
                )
                HairlineDivider(modifier = Modifier.fillMaxWidth())
                DonationOption(
                    title = stringResource(R.string.about_donate_medium),
                    onClick = { onAmountSelected(BillingManager.DonationType.Medium) }
                )
                HairlineDivider(modifier = Modifier.fillMaxWidth())
                DonationOption(
                    title = stringResource(R.string.about_donate_large),
                    onClick = { onAmountSelected(BillingManager.DonationType.Large) }
                )
            }
        }
    }
}

@Composable
private fun DonationOption(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Design.dimensions.spacing.xl)
    ) {
        Text(
            text = title,
            style = Design.textStyles.title,
            color = Design.colors.ink,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Design.colors.inkMuted
        )
    }
}
