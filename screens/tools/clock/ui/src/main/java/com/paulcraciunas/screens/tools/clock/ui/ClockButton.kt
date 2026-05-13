package com.paulcraciunas.screens.tools.clock.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.borderSoft
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun ClockButton(
    remainder: CountdownTimer.Remainder,
    label: String,
    isEnabled: Boolean,
    isLoser: Boolean,
    isSetup: Boolean,
    rotation: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isLoser -> Design.colors.danger
            isEnabled -> Design.colors.primarySoft
            else -> Design.colors.surfaceAlt
        },
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            isLoser -> Design.colors.onPrimary
            isEnabled -> Design.colors.primary
            else -> Design.colors.inkSoft
        },
        label = "contentColor"
    )

    Button(
        onClick = onClick,
        enabled = isEnabled,
        shape = Design.shapes.card,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.6f),
            disabledContentColor = contentColor.copy(alpha = 0.6f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = Design.dimensions.elevation.md,
            pressedElevation = Design.dimensions.elevation.none,
            disabledElevation = Design.dimensions.elevation.none,
        ),
        contentPadding = PaddingValues(Design.dimensions.spacing.xxl),
        border = borderSoft(),
        modifier = modifier
            .fillMaxWidth()
            .padding(Design.dimensions.spacing.sm)
            .rotate(rotation),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label.uppercase(),
                style = Design.typography.labelLarge,
                letterSpacing = 1.2.sp,
            )
            ChessGymSpacer(size = SpacerSize.LARGE)
            Text(
                text = if (isSetup) {
                    stringResource(R.string.clock_tap_to_start)
                } else {
                    formatTime(remainder)
                },
                style = Design.textStyles.displayNumeric,
            )
            if (isLoser) {
                ChessGymSpacer()
                Text(
                    text = stringResource(R.string.clock_time_up),
                    style = Design.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Design.colors.danger,
                )
            }
        }
    }
}

private fun formatTime(remainder: CountdownTimer.Remainder): String {
    val minutes = remainder.seconds / 60
    val seconds = remainder.seconds % 60
    val tenths = (remainder.millis % 1000) / 100

    return when {
        minutes > 0 -> "%d:%02d".format(minutes, seconds)
        else -> "%d.%d".format(seconds, tenths) // Show tenths only when under a minute
    }
}

@Preview(name = "Active Player")
@Preview(name = "Active Player - Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ClockButtonActivePreview() {
    ChessGymTheme {
        Surface {
            ClockButton(
                remainder = CountdownTimer.Remainder(seconds = 300, millis = 0),
                label = "White",
                isEnabled = true,
                isLoser = false,
                isSetup = false,
                rotation = 0f,
                onClick = {},
                modifier = Modifier.height(200.dp)
            )
        }
    }
}

@Preview(name = "Inactive Player")
@Composable
private fun ClockButtonInactivePreview() {
    ChessGymTheme {
        Surface {
            ClockButton(
                remainder = CountdownTimer.Remainder(seconds = 285, millis = 0),
                label = "Black",
                isEnabled = false,
                isLoser = false,
                isSetup = false,
                rotation = 0f,
                onClick = {},
                modifier = Modifier.height(200.dp)
            )
        }
    }
}

@Preview(name = "Setup State")
@Composable
private fun ClockButtonSetupPreview() {
    ChessGymTheme {
        Surface {
            ClockButton(
                remainder = CountdownTimer.Remainder(seconds = 600, millis = 0),
                label = "White",
                isEnabled = true,
                isLoser = false,
                isSetup = true,
                rotation = 0f,
                onClick = {},
                modifier = Modifier.height(200.dp)
            )
        }
    }
}

@Preview(name = "Loser State")
@Preview(name = "Loser State - Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ClockButtonLoserPreview() {
    ChessGymTheme {
        Surface {
            ClockButton(
                remainder = CountdownTimer.Remainder(seconds = 0, millis = 0),
                label = "Black",
                isEnabled = false,
                isLoser = true,
                isSetup = false,
                rotation = 0f,
                onClick = {},
                modifier = Modifier.height(200.dp)
            )
        }
    }
}
