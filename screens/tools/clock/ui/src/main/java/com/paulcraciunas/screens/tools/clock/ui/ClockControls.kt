package com.paulcraciunas.screens.tools.clock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.tools.clock.vm.ClockScreenInteractor
import com.paulcraciunas.screens.tools.clock.vm.ClockUiState

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = uiState is ClockUiState.Setup,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            val setup = uiState as? ClockUiState.Setup
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.testTag { ClockScreenTags.Controls.TIME_SELECTOR },
            ) {
                Text(
                    text = stringResource(R.string.clock_time_control),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag { ClockScreenTags.Controls.INCREMENT_SELECTOR },
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    ClockUiState.AVAILABLE_MINUTES.forEach { minutes ->
                        FilterChip(
                            modifier = Modifier.width(80.dp),
                            selected = setup?.selectedMinutes == minutes,
                            onClick = { interactions.onTimeSelected(minutes) },
                            label = {
                                Text(
                                    text = stringResource(R.string.clock_minutes_format, minutes),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            },
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.clock_increment),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag { ClockScreenTags.Controls.INCREMENT_SELECTOR },
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    ClockUiState.AVAILABLE_INCREMENTS.forEach { seconds ->
                        FilterChip(
                            modifier = Modifier.width(80.dp),
                            selected = setup?.selectedIncrement == seconds,
                            onClick = { interactions.onIncrementSelected(seconds) },
                            label = {
                                Text(
                                    text = stringResource(R.string.clock_seconds_format, seconds),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            },
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
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag { ClockScreenTags.STOP_BUTTON },
            ) {
                Text(
                    text = stringResource(R.string.clock_stop),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        AnimatedVisibility(
            visible = uiState is ClockUiState.Finished,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Button(
                onClick = interactions::onNewGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag { ClockScreenTags.NEW_GAME_BUTTON },
            ) {
                Text(
                    text = stringResource(R.string.clock_new_game),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
