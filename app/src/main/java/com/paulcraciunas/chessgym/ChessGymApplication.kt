package com.paulcraciunas.chessgym

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.paulcraciunas.chessgym.error_reporting.CrashReportTree
import com.paulcraciunas.chessgym.error_reporting.GlobalExceptionHandler
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import com.paulcraciunas.global.qualifiers.ApplicationScope
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ChessGymApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var getNetworkState: GetNetworkState
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var userRepository: UserRepository
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        setupLogging()
        setupCrashReporting()
        getNetworkState.start()
        syncUserData()
    }

    private fun setupLogging() {
        Timber.plant(CrashReportTree.tree())
    }

    private fun setupCrashReporting() {
        // Set handler immediately
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler())
        // Set static metadata immediately
        runCatching {
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("app_version", BuildConfig.APP_VERSION)
                setCustomKey("build_number", BuildConfig.BUILD_NUMBER)
            }
        }
        // Reactively handle consent and user ID
        applicationScope.launch {
            // Combine flows to react to both settings and user changes
            combine(
                appSettingsRepository.appSettings.onStart {
                    emit(AppSettings.default())
                },
                userRepository.userUpdates().onStart {
                    emit(User())
                }
            ) { settings, user ->
                val enabled = settings.crashReportingConsent && !BuildConfig.DEBUG
                val userId = if (enabled) user.deviceId else ""
                enabled to userId
            }.distinctUntilChanged()
                .catch { e -> Timber.e(e, "Crash reporting setup failed") }
                .collect { (enabled, userId) ->
                    FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled
                    FirebaseCrashlytics.getInstance().setUserId(userId)
                }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun syncUserData() {
        applicationScope.launch {
            try {
                userRepository.sync()
            } catch (e: Exception) {
                Timber.w(e, "User data sync on launch failed")
            }
        }
    }
}
