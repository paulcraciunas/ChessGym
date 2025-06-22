package com.paulcraciunas.chessgym.ui.screens.puzzle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.chessgym.R

@Composable
internal fun PuzzleDashboardScreen(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.under_construction),
            modifier = modifier
        )
    }
}
