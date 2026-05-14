package com.paulcraciunas.screens.tools.clock.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.components.EyebrowType
import com.paulcraciunas.screens.common.design.components.OutlineSegmentButton
import com.paulcraciunas.screens.common.design.components.PrimaryButton
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.clock.vm.ClockScreenInteractor
import com.paulcraciunas.screens.tools.clock.vm.ClockUiState
import com.paulcraciunas.screens.tools.clock.vm.StubClockScreenInteractor

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClockControls(
    uiState: ClockUiState,
    interactions: ClockScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Design.dimensions.spacing.xxl,
                vertical = Design.dimensions.spacing.sm,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = uiState is ClockUiState.Setup,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            val setup = uiState as? ClockUiState.Setup
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
                modifier = Modifier.testTag { ClockScreenTags.Controls.TIME_SELECTOR },
            ) {
                Eyebrow(
                    text = stringResource(R.string.clock_time_control),
                    modifier = Modifier.testTag { ClockScreenTags.Controls.INCREMENT_SELECTOR },
                    type = EyebrowType.Soft,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ClockUiState.AVAILABLE_MINUTES.forEach { minutes ->
                        OutlineSegmentButton(
                            text = stringResource(R.string.clock_minutes_format, minutes),
                            selected = setup?.selectedMinutes == minutes,
                            onClick = { interactions.onTimeSelected(minutes) },
                            modifier = Modifier.width(Design.dimensions.sizes.timeControl),
                        )
                    }
                }
                Eyebrow(
                    text = stringResource(R.string.clock_increment),
                    modifier = Modifier.testTag { ClockScreenTags.Controls.INCREMENT_SELECTOR },
                    type = EyebrowType.Soft,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ClockUiState.AVAILABLE_INCREMENTS.forEach { seconds ->
                        OutlineSegmentButton(
                            text = stringResource(R.string.clock_seconds_format, seconds),
                            selected = setup?.selectedIncrement == seconds,
                            onClick = { interactions.onIncrementSelected(seconds) },
                            modifier = Modifier.width(Design.dimensions.sizes.timeControl),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState is ClockUiState.Playing,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Button(
                onClick = interactions::onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Design.colors.danger,
                    contentColor = Design.colors.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Design.dimensions.spacing.sm)
                    .testTag { ClockScreenTags.STOP_BUTTON },
            ) {
                Text(
                    text = stringResource(R.string.clock_stop),
                    style = Design.typography.titleMedium,
                )
            }
        }

        AnimatedVisibility(
            visible = uiState is ClockUiState.Finished,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            PrimaryButton(
                text = stringResource(R.string.clock_new_game),
                onClick = interactions::onNewGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Design.dimensions.spacing.sm)
                    .testTag { ClockScreenTags.NEW_GAME_BUTTON },
            )
        }
    }
}

@Preview(name = "Clock Controls - Setup")
@Preview(name = "Clock Controls - Setup (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ClockControlsSetupPreview() {
    ChessGymTheme {
        ClockControls(
            uiState = ClockUiState.Setup(),
            interactions = StubClockScreenInteractor(),
        )
    }
}

@Preview(name = "Clock Controls - Playing")
@Composable
private fun ClockControlsPlayingPreview() {
    ChessGymTheme {
        ClockControls(
            uiState = ClockUiState.Playing(
                whiteTime = CountdownTimer.Remainder(120, 0),
                blackTime = CountdownTimer.Remainder(150, 0),
                activePlayer = Side.WHITE,
            ),
            interactions = StubClockScreenInteractor(),
        )
    }
}

@Preview(name = "Clock Controls - Finished")
@Composable
private fun ClockControlsFinishedPreview() {
    ChessGymTheme {
        ClockControls(
            uiState = ClockUiState.Finished(
                whiteTime = CountdownTimer.Remainder(0, 0),
                blackTime = CountdownTimer.Remainder(120, 0),
                loser = Side.WHITE,
            ),
            interactions = StubClockScreenInteractor(),
        )
    }
}
