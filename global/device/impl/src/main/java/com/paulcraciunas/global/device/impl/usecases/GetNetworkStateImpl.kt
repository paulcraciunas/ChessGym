package com.paulcraciunas.global.device.impl.usecases

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetNetworkStateImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GetNetworkState {

    private val _networkState = MutableSharedFlow<GetNetworkState.NetworkState>(replay = 1)
    private val networkState = _networkState.asSharedFlow()
    
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkStateCallback
    private var isStarted = false

    override fun start() {
        if (isStarted) return

        context.getSystemService<ConnectivityManager>()?.let {
            connectivityManager = it
        } ?: {
            _networkState.tryEmit(GetNetworkState.NetworkState.Unknown)
        }
        if (!::connectivityManager.isInitialized) {
            Timber.e("Failed to initialize ConnectivityManager")
            return
        }

        // Emit current state immediately
        _networkState.tryEmit(getCurrentNetworkState())

        // Register callback for future changes
        networkCallback = NetworkStateCallback(connectivityManager, _networkState)
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        isStarted = true
    }

    override operator fun invoke(): Flow<GetNetworkState.NetworkState> = 
        networkState.distinctUntilChanged()

    private fun getCurrentNetworkState(): GetNetworkState.NetworkState {
        val connectivityManager = this.connectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return GetNetworkState.NetworkState.Disconnected

        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return GetNetworkState.NetworkState.Disconnected

        return when {
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> {
                GetNetworkState.NetworkState.Connected
            }
            else -> GetNetworkState.NetworkState.Disconnected
        }
    }
}
