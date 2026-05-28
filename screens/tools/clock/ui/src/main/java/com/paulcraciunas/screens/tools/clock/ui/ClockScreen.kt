package com.paulcraciunas.screens.tools.clock.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.media.AudioManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.clock.vm.ClockUiState
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    uiState: ClockUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onWhiteTapped: () -> Unit = {},
    onBlackTapped: () -> Unit = {},
    onStop: () -> Unit = {},
    onNewGame: () -> Unit = {},
    onTimeSelected: (minutes: Int) -> Unit = {},
    onIncrementSelected: (increment: Int) -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val isInspectionMode = LocalInspectionMode.current

    val triggerFeedback = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    if (uiState is ClockUiState.Finished && !isInspectionMode) {
        // Move ToneGenerator logic to a separate object to avoid NoClassDefFoundError in Previews
        // as ToneGenerator might not be available in the layoutlib classpath.
        TonePlayer.PlayFinishedSound(triggerFeedback)
    }

    Scaffold(
        topBar = { ChildAppBar(onBack = onNavigateBack, title = stringResource(R.string.clock_title)) },
        containerColor = Design.colors.bg,
        modifier = modifier.testTag { ClockScreenTags.SCREEN },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val isSetup = uiState is ClockUiState.Setup
            val isFinished = uiState is ClockUiState.Finished
            val blackEnabled = uiState is ClockUiState.Playing && uiState.activePlayer == Side.BLACK
            val whiteEnabled = isSetup || (uiState is ClockUiState.Playing && uiState.activePlayer == Side.WHITE)
            ClockButton(
                remainder = uiState.blackTime,
                label = stringResource(R.string.clock_black),
                isEnabled = blackEnabled,
                isLoser = isFinished && uiState.loser == Side.BLACK,
                isSetup = isSetup,
                rotation = 180f,
                onClick = {
                    triggerFeedback()
                    onBlackTapped()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag { ClockScreenTags.BLACK_BUTTON },
            )
            ClockButton(
                remainder = uiState.whiteTime,
                label = stringResource(R.string.clock_white),
                isEnabled = whiteEnabled,
                isLoser = isFinished && uiState.loser == Side.WHITE,
                isSetup = isSetup,
                rotation = 0f,
                onClick = {
                    triggerFeedback()
                    onWhiteTapped()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag { ClockScreenTags.WHITE_BUTTON },
            )
            ClockControls(
                uiState = uiState,
                onStop = onStop,
                onNewGame = onNewGame,
                onTimeSelected = onTimeSelected,
                onIncrementSelected = onIncrementSelected,
            )
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
            onNavigateBack = {},
        )
    }
}
