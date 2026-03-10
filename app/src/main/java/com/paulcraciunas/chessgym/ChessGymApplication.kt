package com.paulcraciunas.chessgym

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.paulcraciunas.chessgym.error_reporting.CrashReportTree
import com.paulcraciunas.chessgym.error_reporting.GlobalExceptionHandler
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ChessGymApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var getNetworkState: GetNetworkState

    override fun onCreate() {
        super.onCreate()

        Timber.plant(CrashReportTree.tree())
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler())
        getNetworkState.start() // Start network state monitoring
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
