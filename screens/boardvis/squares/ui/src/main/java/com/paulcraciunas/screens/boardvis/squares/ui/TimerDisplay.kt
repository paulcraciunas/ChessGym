package com.paulcraciunas.screens.boardvis.squares.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun TimerDisplay(
    seconds: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        seconds <= 5 -> MaterialTheme.colorScheme.error
        seconds <= 10 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = formatTime(seconds),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
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
        TimerDisplay(seconds = 25)
    }
}

@Preview
@Composable
private fun TimerDisplayWarningPreview() {
    ChessGymTheme {
        TimerDisplay(seconds = 8)
    }
}

@Preview
@Composable
private fun TimerDisplayCriticalPreview() {
    ChessGymTheme {
        TimerDisplay(seconds = 3)
    }
}
