package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun MoveNavigationControls(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onJumpToStart: () -> Unit,
    onPreviousMove: () -> Unit,
    onNextMove: () -> Unit,
    onJumpToEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xgut),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onJumpToStart,
            enabled = canGoBack,
            modifier = Modifier.testTag { MoveNavigationTags.JUMP_TO_START },
        ) {
            Icon(
                painter = painterResource(R.drawable.keyboard_double_arrow_left),
                contentDescription = stringResource(R.string.import_game_jump_to_start),
                modifier = Modifier.size(Design.dimensions.spacing.section),
            )
        }
        IconButton(
            onClick = onPreviousMove,
            enabled = canGoBack,
            modifier = Modifier.testTag { MoveNavigationTags.PREVIOUS },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.import_game_previous_move),
                modifier = Modifier.size(Design.dimensions.spacing.section),
            )
        }
        IconButton(
            onClick = onNextMove,
            enabled = canGoForward,
            modifier = Modifier.testTag { MoveNavigationTags.NEXT },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.import_game_next_move),
                modifier = Modifier.size(Design.dimensions.spacing.section),
            )
        }
        IconButton(
            onClick = onJumpToEnd,
            enabled = canGoForward,
            modifier = Modifier.testTag { MoveNavigationTags.JUMP_TO_END },
        ) {
            Icon(
                painter = painterResource(R.drawable.keyboard_double_arrow_right),
                contentDescription = stringResource(R.string.import_game_jump_to_end),
                modifier = Modifier.size(Design.dimensions.spacing.section),
            )
        }
    }
}

object MoveNavigationTags {
    const val JUMP_TO_START = "nav_jump_to_start"
    const val PREVIOUS = "nav_previous"
    const val NEXT = "nav_next"
    const val JUMP_TO_END = "nav_jump_to_end"
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun MoveNavigationAllEnabledPreview() {
    ChessGymTheme {
        MoveNavigationControls(
            canGoBack = true,
            canGoForward = true,
            onJumpToStart = {},
            onPreviousMove = {},
            onNextMove = {},
            onJumpToEnd = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MoveNavigationAtStartPreview() {
    ChessGymTheme {
        MoveNavigationControls(
            canGoBack = false,
            canGoForward = true,
            onJumpToStart = {},
            onPreviousMove = {},
            onNextMove = {},
            onJumpToEnd = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MoveNavigationAtEndPreview() {
    ChessGymTheme {
        MoveNavigationControls(
            canGoBack = true,
            canGoForward = false,
            onJumpToStart = {},
            onPreviousMove = {},
            onNextMove = {},
            onJumpToEnd = {},
        )
    }
}
