package com.paulcraciunas.screens.tools.clock.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import androidx.compose.ui.semantics.semantics
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.backgroundColor
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.clock.vm.ClockScreenInteractor
import com.paulcraciunas.screens.tools.clock.vm.ClockUiState
import com.paulcraciunas.screens.tools.clock.vm.StubClockScreenInteractor
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    uiState: ClockUiState,
    interactions: ClockScreenInteractor,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val isInspectionMode = LocalInspectionMode.current

    val triggerFeedback = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    if (uiState is ClockUiState.Finished && !isInspectionMode) {
        // Move ToneGenerator logic to a separate object to avoid NoClassDefFoundError in Previews
        // as ToneGenerator might not be available in the layoutlib classpath.
        TonePlayer.PlayFinishedSound(triggerFeedback)
    }

    val bgColor = MaterialTheme.colorScheme.background
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.clock_title),
                navButton = { Back(onClick = onNavigateBack) },
            )
        },
        modifier = modifier
            .testTag { ClockScreenTags.SCREEN }
            .semantics { this.backgroundColor = bgColor },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            val players = listOf(Side.BLACK, Side.WHITE)
            players.forEachIndexed { index, side ->
                val isActive = uiState is ClockUiState.Playing && uiState.activePlayer == side
                val isLoser = uiState is ClockUiState.Finished && uiState.loser == side
                val isSetup = uiState is ClockUiState.Setup

                ClockButton(
                    remainder = if (side == Side.BLACK) uiState.blackTime else uiState.whiteTime,
                    label = stringResource(if (side == Side.BLACK) R.string.clock_black else R.string.clock_white),
                    isEnabled = isSetup || isActive,
                    isLoser = isLoser,
                    isSetup = isSetup,
                    rotation = if (side == Side.BLACK) 180f else 0f,
                    onClick = {
                        triggerFeedback()
                        if (side == Side.BLACK) interactions.onBlackTapped() else interactions.onWhiteTapped()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag { if (side == Side.BLACK) ClockScreenTags.BLACK_BUTTON else ClockScreenTags.WHITE_BUTTON },
                )

                if (index == 0) Spacer(modifier = Modifier.height(8.dp))
            }

            ClockControls(uiState = uiState, interactions = interactions)
        }
    }
}

/**
 * Helper object to isolate ToneGenerator usage.
 * This prevents the main ClockScreenKt class from failing to load in Compose Previews
 * due to NoClassDefFoundError: android/media/ToneGenerator.
 */
private object TonePlayer {
    @Composable
    fun PlayFinishedSound(triggerFeedback: () -> Unit) {
        val toneGenerator = remember {
            try {
                android.media.ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            } catch (t: Throwable) {
                Timber.w(t, "ToneGenerator unavailable")
                null
            }
        }
        LaunchedEffect(Unit) {
            triggerFeedback()
            toneGenerator?.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 500)
        }
        DisposableEffect(Unit) {
            onDispose {
                toneGenerator?.release()
            }
        }
    }
}

@Preview("Clock - Setup")
@Preview("Clock - Setup (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ClockSetupPreview() {
    ChessGymTheme {
        ClockScreen(
            uiState = ClockUiState.Setup(),
            interactions = StubClockScreenInteractor(),
            onNavigateBack = {},
        )
    }
}

@Preview("Clock - Playing")
@Composable
private fun ClockPlayingPreview() {
    ChessGymTheme {
        ClockScreen(
            uiState = ClockUiState.Playing(
                whiteTime = CountdownTimer.Remainder(245, millis = 0),
                blackTime = CountdownTimer.Remainder(280, millis = 0),
                activePlayer = Side.WHITE,
            ),
            interactions = StubClockScreenInteractor(),
            onNavigateBack = {},
        )
    }
}

@Preview("Clock - Finished")
@Composable
private fun ClockFinishedPreview() {
    ChessGymTheme {
        ClockScreen(
            uiState = ClockUiState.Finished(
                whiteTime = CountdownTimer.Remainder(0, millis = 0),
                blackTime = CountdownTimer.Remainder(180, millis = 0),
                loser = Side.WHITE,
            ),
            interactions = StubClockScreenInteractor(),
            onNavigateBack = {},
        )
    }
}
