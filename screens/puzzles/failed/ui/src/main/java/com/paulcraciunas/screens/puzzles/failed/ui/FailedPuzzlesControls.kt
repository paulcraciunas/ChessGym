package com.paulcraciunas.screens.puzzles.failed.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun Back(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.navigate_back),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BackPreview() {
    ChessGymTheme {
        Back(onClick = {})
    }
}
