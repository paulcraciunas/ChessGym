package com.paulcraciunas.global.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationDispatcher @Inject constructor() {
    private val _navigationEvents = Channel<Destination>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    fun navigate(to: Destination) {
        _navigationEvents.trySend(element = to)
    }

    sealed class Destination {
        data class Analysis(val puzzleId: Int) : Destination()
    }
}
