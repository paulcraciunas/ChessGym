package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design

/** A 1px hairline divider in the theme's divider colour. */
@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .height(1.dp)
        .background(Design.colors.divider))
}
