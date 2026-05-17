package com.paulcraciunas.screens.loading.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.components.OutlineSegmentButton
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.loading.vm.DatabaseTier

@Composable
internal fun TierSelector(
    selectedTier: DatabaseTier,
    onTierSelected: (DatabaseTier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.md),
    ) {
        Eyebrow(text = stringResource(R.string.landing_tier_label))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        ) {
            DatabaseTier.entries.forEach { tier ->
                OutlineSegmentButton(
                    text = stringResource(tier.label()),
                    selected = tier == selectedTier,
                    onClick = { onTierSelected(tier) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Text(
            text = stringResource(selectedTier.description()),
            style = Design.typography.bodySmall,
            color = Design.colors.inkMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@StringRes
private fun DatabaseTier.label(): Int = when (this) {
    DatabaseTier.Full -> R.string.loading_tier_complete
    DatabaseTier.Compact -> R.string.loading_tier_compact
    DatabaseTier.Lite -> R.string.loading_tier_lite
}

@StringRes
private fun DatabaseTier.description(): Int = when (this) {
    DatabaseTier.Full -> R.string.loading_tier_description_complete
    DatabaseTier.Compact -> R.string.loading_tier_description_compact
    DatabaseTier.Lite -> R.string.loading_tier_description_lite
}
