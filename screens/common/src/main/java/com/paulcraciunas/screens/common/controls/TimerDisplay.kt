package com.paulcraciunas.screens.common.controls

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R.drawable
import com.paulcraciunas.screens.common.design.components.ChessGymChip
import com.paulcraciunas.screens.common.design.components.ChipTone
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.data.RemainingTime

@Composable
fun TimerDisplay(
    seconds: Int,
    modifier: Modifier = Modifier
) {
    val tone = when {
        seconds <= 20 -> ChipTone.Failed
        seconds <= 60 -> ChipTone.Accent
        else -> ChipTone.Soft
    }

    ChessGymChip(
        text = formatTime(seconds),
        tone = tone,
        leadingIcon = ImageVector.vectorResource(drawable.clock_icon),
        modifier = modifier,
    )
}

@Composable
fun TimerDisplay(
    remainingTime: RemainingTime,
    modifier: Modifier = Modifier
) {
    ChessGymChip(
        text = remainingTime.value,
        tone = if (remainingTime.danger) ChipTone.Failed else ChipTone.Accent,
        leadingIcon = ImageVector.vectorResource(drawable.clock_icon),
        modifier = modifier.padding(Design.dimensions.spacing.xl),
    )
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview
@Composable
private fun TimerDisplayNormalPreview() {
    ChessGymTheme {
        TimerDisplay(seconds = 45)
    }
}

@Preview
@Composable
private fun TimerDisplayWarningPreview() {
    ChessGymTheme {
        TimerDisplay(seconds = 25)
    }
}

@Preview
@Composable
private fun TimerDisplayCriticalPreview() {
    ChessGymTheme {
        TimerDisplay(seconds = 5)
    }
}
