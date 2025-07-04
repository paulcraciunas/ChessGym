package com.paulcraciunas.global.device.impl.usecases

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import kotlinx.coroutines.flow.MutableSharedFlow

internal class NetworkStateCallback(
    private val connectivityManager: ConnectivityManager,
    private val networkStateFlow: MutableSharedFlow<GetNetworkState.NetworkState>
) : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        val state = if (networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            GetNetworkState.NetworkState.Connected
        } else {
            GetNetworkState.NetworkState.Disconnected
        }
        networkStateFlow.tryEmit(state)
    }

    override fun onLost(network: Network) {
        networkStateFlow.tryEmit(GetNetworkState.NetworkState.Disconnected)
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        val state = if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            GetNetworkState.NetworkState.Connected
        } else {
            GetNetworkState.NetworkState.Disconnected
        }
        networkStateFlow.tryEmit(state)
    }
}
