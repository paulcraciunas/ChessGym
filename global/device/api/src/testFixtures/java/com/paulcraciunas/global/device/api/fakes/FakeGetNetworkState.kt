package com.paulcraciunas.global.device.api.fakes

import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeGetNetworkState : GetNetworkState {
    private val _networkState = MutableStateFlow<GetNetworkState.NetworkState>(GetNetworkState.NetworkState.Connected)

    fun setState(state: GetNetworkState.NetworkState) {
        _networkState.value = state
    }

    override fun start() {
        // No-op for fake - network state is controlled manually via setState()
    }

    override fun invoke(): Flow<GetNetworkState.NetworkState> = _networkState.asStateFlow()
}
