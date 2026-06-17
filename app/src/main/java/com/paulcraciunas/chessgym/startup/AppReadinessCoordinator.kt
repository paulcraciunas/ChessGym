package com.paulcraciunas.chessgym.startup

import com.paulcraciunas.global.qualifiers.ApplicationScope
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppReadinessCoordinator @Inject constructor(
    appSettingsRepository: AppSettingsRepository,
    userRepository: UserRepository,
    resourcePreWarmer: ResourcePreWarmer,
    checkDeviceRestore: IDeviceRestoreCheck,
    @ApplicationScope scope: CoroutineScope,
) {
    private val isIntegrityCheckComplete = MutableStateFlow(false)

    val uiState: StateFlow<ReadinessState> = combine(
        appSettingsRepository.appSettings,
        userRepository.userUpdates().map { true },
        resourcePreWarmer.isComplete,
        isIntegrityCheckComplete,
    ) { appSettings, _, preWarmed, integrityCheck ->
        ReadinessState(
            isReady = preWarmed && integrityCheck,
            puzzlesDownloaded = appSettings.puzzlesDownloaded,
            lightMode = appSettings.lightMode,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = ReadinessState()
    )

    init {
        scope.launch {
            checkDeviceRestore()
            isIntegrityCheckComplete.value = true
        }
    }
}

data class ReadinessState(
    val isReady: Boolean = false,
    val puzzlesDownloaded: Boolean = false,
    val lightMode: AppSettings.LightMode = AppSettings.LightMode.System,
)
