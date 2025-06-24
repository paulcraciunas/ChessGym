package com.paulcraciunas.global.device.api.usecases

interface GetNetworkState {
    operator fun invoke(): NetworkState

    sealed class NetworkState {
        data object Connected : NetworkState()
        data object Disconnected : NetworkState()
        data object Unknown : NetworkState()
    }
}
