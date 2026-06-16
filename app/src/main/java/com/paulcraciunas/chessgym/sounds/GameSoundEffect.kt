package com.paulcraciunas.chessgym.sounds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.global.sounds.SoundManager
import com.paulcraciunas.screens.common.LocalUiSettings

@Composable
internal fun GameSoundEffects(
    soundManager: SoundManager,
    soundEvents: SoundCoordinator,
) {
    val playSoundsEnabled = LocalUiSettings.current.playSounds

    LaunchedEffect(soundEvents.soundEvents, playSoundsEnabled) {
        if (!playSoundsEnabled) return@LaunchedEffect

        soundEvents.soundEvents.collect { event ->
            when (event) {
                is SoundCoordinator.SoundEvent.GameStart -> soundManager.playGameStart()
                is SoundCoordinator.SoundEvent.GameOver -> soundManager.playGameOver()
                is SoundCoordinator.SoundEvent.Move -> soundManager.playMove()
                is SoundCoordinator.SoundEvent.Tick -> soundManager.startTickCountdown(event.durationSeconds)
            }
        }
    }
}
