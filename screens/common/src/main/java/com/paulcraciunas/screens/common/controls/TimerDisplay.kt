package com.paulcraciunas.screens.common.controls

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun TimerDisplay(
    seconds: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        seconds <= 10 -> Design.colors.danger
        seconds <= 20 -> Design.colors.accent
        else -> Design.colors.ink
    }

    Text(
        text = formatTime(seconds),
        style = Design.textStyles.monoTimer,
        color = color,
        modifier = modifier
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
        TimerDisplay(seconds = 15)
    }
}

@Preview
@Composable
private fun TimerDisplayCriticalPreview() {
    ChessGymTheme {
        TimerDisplay(seconds = 5)
    }
}
