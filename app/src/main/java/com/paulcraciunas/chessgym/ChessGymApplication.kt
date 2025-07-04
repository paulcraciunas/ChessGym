package com.paulcraciunas.chessgym

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ChessGymApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var getNetworkState: GetNetworkState

    override fun onCreate() {
        super.onCreate()

        getNetworkState.start() // Start network state monitoring
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
