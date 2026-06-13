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

    enum class SoundEvent {
        GameStart,
        GameOver,
        Move,
        Tick
    }
}
