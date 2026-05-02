package com.paulcraciunas.chessgym

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.paulcraciunas.chessgym.error_reporting.CrashReportTree
import com.paulcraciunas.chessgym.error_reporting.GlobalExceptionHandler
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ChessGymApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var getNetworkState: GetNetworkState
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var userRepository: UserRepository

    // Use a dedicated scope for application-level background tasks
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        setupLogging()
        setupCrashReporting()
        getNetworkState.start()
    }

    private fun setupLogging() {
        Timber.plant(CrashReportTree.tree())
    }

    private fun setupCrashReporting() {
        // Set handler immediately
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler())
        // Set static metadata immediately
        Firebase.crashlytics.apply {
            setCustomKey("app_version", BuildConfig.APP_VERSION)
            setCustomKey("build_number", BuildConfig.BUILD_NUMBER)
        }
        // Reactively handle consent and user ID
        applicationScope.launch {
            // Combine flows to react to both settings and user changes
            combine(
                appSettingsRepository.appSettings,
                userRepository.userUpdates()
            ) { settings, user ->
                settings to user
            }.collect { (settings, user) ->
                val enabled = settings.crashReportingConsent && !BuildConfig.DEBUG
                Firebase.crashlytics.isCrashlyticsCollectionEnabled = enabled
                if (enabled) {
                    Firebase.crashlytics.setUserId(user.deviceId)
                }
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
