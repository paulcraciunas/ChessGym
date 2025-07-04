package com.paulcraciunas.global.device.api.usecases

import kotlinx.coroutines.flow.Flow

interface GetNetworkState {
    fun start()
    operator fun invoke(): Flow<NetworkState>

    sealed class NetworkState {
        data object Connected : NetworkState()
        data object Disconnected : NetworkState()
        data object Unknown : NetworkState()
    }
}
