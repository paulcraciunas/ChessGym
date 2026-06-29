package com.paulcraciunas.chessgym.startup

import com.paulcraciunas.chessgym.BuildConfig
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class AppSettingsProvisioningImpl @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
) : AppSettingsProvisioning {

    override operator fun invoke(): Flow<AppSettings> = flow {
        @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
        if (BuildConfig.BUILD_TYPE == "benchmark" || BuildConfig.BUILD_TYPE == "baselineProfile") {
            appSettingsRepository.updatePuzzlesDownloaded(true)
            appSettingsRepository.updateMinPuzzleRating(400)
            appSettingsRepository.updateMaxPuzzleRating(3000)
            appSettingsRepository.updateTotalPuzzleCount(26_010)
        }
        emit(Unit)
    }.flatMapLatest {
        appSettingsRepository.appSettings
    }
}
