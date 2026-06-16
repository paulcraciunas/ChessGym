package com.paulcraciunas.screens.common.controls

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R.drawable
import com.paulcraciunas.screens.common.design.components.ChessGymChip
import com.paulcraciunas.screens.common.design.components.ChipStyle
import com.paulcraciunas.screens.common.design.components.ChipTone
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.data.RemainingTime

@Composable
fun TimerDisplay(
    remainingTime: RemainingTime,
    modifier: Modifier = Modifier,
) {
    ChessGymChip(
        text = remainingTime.value,
        tone = if (remainingTime.danger) ChipTone.Failed else ChipTone.Accent,
        style = ChipStyle.Timer,
        leadingIcon = ImageVector.vectorResource(drawable.clock_icon),
        modifier = modifier.padding(Design.dimensions.spacing.xl),
    )
}

@Preview
@Composable
private fun TimerDisplayNormalPreview() {
    ChessGymTheme {
        TimerDisplay(remainingTime = RemainingTime(value = "45.0", danger = false))
    }
}

@Preview
@Composable
private fun TimerDisplayWarningPreview() {
    ChessGymTheme {
        TimerDisplay(remainingTime = RemainingTime(value = "02:42", danger = false))
    }
}

@Preview
@Composable
private fun TimerDisplayCriticalPreview() {
    ChessGymTheme {
        TimerDisplay(remainingTime = RemainingTime(value = "15.2", danger = true))
    }
}
