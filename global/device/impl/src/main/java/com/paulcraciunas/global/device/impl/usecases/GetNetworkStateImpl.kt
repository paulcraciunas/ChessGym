package com.paulcraciunas.global.device.impl.usecases

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GetNetworkStateImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GetNetworkState {

    override operator fun invoke(): GetNetworkState.NetworkState {
        val connectivityManager = context.getSystemService<ConnectivityManager>() ?: return GetNetworkState.NetworkState.Unknown
        val activeNetwork = connectivityManager.activeNetwork ?: return GetNetworkState.NetworkState.Disconnected

        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return GetNetworkState.NetworkState.Disconnected

        return when {
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> GetNetworkState.NetworkState.Connected
            else -> GetNetworkState.NetworkState.Disconnected
        }
    }
}
