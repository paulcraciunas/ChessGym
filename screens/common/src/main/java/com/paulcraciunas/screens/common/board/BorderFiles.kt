package com.paulcraciunas.screens.common.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.theme.BoardColors


@Composable
internal fun BorderFiles(
    orientation: BoardOrientation,
    modifier: Modifier = Modifier,
    height: Dp = 15.dp
) {
    Row(
        modifier = modifier
            .requiredHeight(height)
    ) {
        Box(modifier = Modifier.requiredWidth(height)) // Empty corner
        for (file in orientation.files) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .requiredHeight(height),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = file.name,
                    color = BoardColors.boardText,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Box(modifier = Modifier.requiredWidth(height)) // Empty corner
    }
}
