package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.global.sounds.SoundCoordinator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun Flow<SingleSessionState>.toSoundEvents(): Flow<SoundCoordinator.SoundEvent> = flow {
    var previous: SingleSessionState? = null

    collect { current ->
        val prev = previous
        if (prev != null) {
            if (current.status == SingleSessionState.Status.Ready && prev.status != SingleSessionState.Status.Ready) {
                emit(SoundCoordinator.SoundEvent.GameStart)
            }
            if (current.status == SingleSessionState.Status.GameOver && prev.status != SingleSessionState.Status.GameOver) {
                emit(SoundCoordinator.SoundEvent.GameOver)
            }
            if (current.boardState.movePlayed && !prev.boardState.movePlayed) {
                emit(SoundCoordinator.SoundEvent.Move)
            }
        }
        previous = current
    }
}

fun Flow<PlaySessionState>.toSoundEvents(timeThresholdMs: Long? = null): Flow<SoundCoordinator.SoundEvent> = flow {
    var previous: PlaySessionState? = null
    var ticking = false

    collect { current ->
        val prev = previous
        if (prev != null) {
            if (current.status == PlaySessionState.Status.Ready && prev.status != PlaySessionState.Status.Ready) {
                emit(SoundCoordinator.SoundEvent.GameStart)
                ticking = false // reset the flag, in case it might be needed later
            }
            if (current.status == PlaySessionState.Status.Ended && prev.status != PlaySessionState.Status.Ended) {
                emit(SoundCoordinator.SoundEvent.GameOver)
            }
            if (current.boardState.movePlayed && !prev.boardState.movePlayed) {
                emit(SoundCoordinator.SoundEvent.Move)
            }
            if (current.remainingTimeMs != null && timeThresholdMs != null) {
                if (current.remainingTimeMs <= timeThresholdMs && current.status == PlaySessionState.Status.Playing && !ticking) {
                    ticking = true
                    emit(SoundCoordinator.SoundEvent.Tick(durationSeconds = (timeThresholdMs / 1000).toInt()))
                }
            }
        }
        previous = current
    }
}
