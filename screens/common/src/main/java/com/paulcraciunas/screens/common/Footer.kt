package com.paulcraciunas.screens.common

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.BrassRule
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun Footer(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        BrassRule(
            modifier = Modifier.padding(
                horizontal = Design.dimensions.spacing.section,
                vertical = Design.dimensions.spacing.xxl,
            ),
        )
        Image(
            painter = painterResource(R.drawable.knight_white),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Design.colors.primary),
            modifier = Modifier.size(Design.dimensions.sizes.hitTarget),
        )
        ChessGymSpacer(size = SpacerSize.SMALL)
        Text(
            text = stringResource(R.string.app_name),
            style = Design.typography.titleMedium,
            color = Design.colors.primary,
        )
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FooterPreview() {
    ChessGymTheme {
        Footer()
    }
}
