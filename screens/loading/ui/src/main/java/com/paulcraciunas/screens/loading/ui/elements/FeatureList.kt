package com.paulcraciunas.screens.loading.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.IconBadge
import com.paulcraciunas.screens.common.design.components.IconBorderType
import com.paulcraciunas.screens.common.design.components.IconSize
import com.paulcraciunas.screens.common.design.components.IconStyle
import com.paulcraciunas.screens.common.design.components.IconTintType
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
internal fun FeatureList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
    ) {
        FeatureItem(text = stringResource(R.string.landing_feature_puzzles))
        FeatureItem(text = stringResource(R.string.landing_feature_offline))
        FeatureItem(text = stringResource(R.string.landing_feature_rating))
    }
}

@Composable
private fun FeatureItem(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
    ) {
        IconBadge(
            imageVector = Icons.Default.Check,
            style = IconStyle.Circle,
            borderType = IconBorderType.None,
            tint = IconTintType.Accent,
            size = IconSize.Small
        )
        Text(
            text = text,
            style = Design.typography.bodyLarge,
            color = Design.colors.inkSoft,
        )
    }
}
