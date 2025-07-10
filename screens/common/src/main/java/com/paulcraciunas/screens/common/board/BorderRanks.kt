package com.paulcraciunas.screens.common.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
internal fun BorderRanks(
    orientation: BoardOrientation,
    modifier: Modifier = Modifier,
    width: Dp = 15.dp
) {
    Column(
        modifier = modifier
            .requiredWidth(width)
    ) {
        Box(modifier = Modifier.requiredHeight(width)) // Empty corner
        for (rank in orientation.ranks) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .requiredWidth(width),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.name,
                    color = BoardColors.boardText,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Box(modifier = Modifier.requiredHeight(width)) // Empty corner
    }
}
