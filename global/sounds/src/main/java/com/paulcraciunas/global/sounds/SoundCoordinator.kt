package com.paulcraciunas.global.sounds

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundCoordinator @Inject constructor() {
    private val _events = Channel<SoundEvent>(Channel.BUFFERED)
    val soundEvents = _events.receiveAsFlow()

    fun trigger(event: SoundEvent) {
        _events.trySend(element = event)
    }

    sealed class SoundEvent {
        data object GameStart : SoundEvent()
        data object GameOver : SoundEvent()
        data object Move : SoundEvent()
        data class Tick(val durationSeconds: Int) : SoundEvent()
    }
}
