package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun DefaultButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes text: Int = R.string.boardvis_play,
    icon: ImageVector? = null,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
    ) {
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Design.dimensions.sizes.icon)
            )
            ChessGymSpacer(size = SpacerSize.SMALL)
        }
        Text(
            text = stringResource(text),
            style = Design.typography.titleMedium,
            modifier = Modifier.padding(vertical = Design.dimensions.spacing.xs)
        )
    }
}

@Composable
fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes text: Int = R.string.boardvis_play,
) {
    DefaultButton(
        onClick = onClick,
        modifier = modifier,
        text = text,
        icon = Icons.Filled.PlayArrow
    )
}

@Composable
fun RefreshButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes text: Int = R.string.boardvis_play_again,
) {
    DefaultButton(
        onClick = onClick,
        modifier = modifier,
        text = text,
        icon = Icons.Filled.Refresh
    )
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PlayButtonPreview() {
    ChessGymTheme {
        PlayButton(onClick = {})
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun RefreshButtonPreview() {
    ChessGymTheme {
        RefreshButton(onClick = {})
    }
}
