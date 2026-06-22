package com.paulcraciunas.screens.common.controls

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
fun FlipBoardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.swap_vert_icon),
            contentDescription = stringResource(R.string.tools_analysis_flip_board),
            tint = Design.colors.accent,
        )
    }
}
