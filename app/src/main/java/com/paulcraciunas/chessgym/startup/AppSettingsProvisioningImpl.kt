package com.paulcraciunas.chessgym.startup

import android.content.Context
import com.paulcraciunas.chessgym.BuildConfig
import com.paulcraciunas.puzzles.di.DbName
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class AppSettingsProvisioningImpl @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val userRepository: UserRepository,
    @param:ApplicationContext private val context: Context,
    @param:DbName private val databaseName: String,
) : AppSettingsProvisioning {

    override operator fun invoke(): Flow<AppSettings> = flow {
        try {
            @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
            if (BuildConfig.BUILD_TYPE == "benchmark") {
                appSettingsRepository.updatePuzzlesDownloaded(true) // bypass puzzle DB provisioning
                appSettingsRepository.updateMinPuzzleRating(400)
                appSettingsRepository.updateMaxPuzzleRating(2700)
                appSettingsRepository.updateTotalPuzzleCount(1_000_000)
            } else {
                val settings = appSettingsRepository.appSettings.first()
                if (settings.puzzlesDownloaded) {
                    val databaseFile = context.getDatabasePath(databaseName)
                    if (!databaseFile.exists()) {
                        // A device restore happened. The local DB asset was excluded,
                        // but the old DataStore flags and user state were restored.
                        Timber.w("Puzzle database file missing. Resetting states.")

                        appSettingsRepository.updatePuzzlesDownloaded(false)
                        val currentUser = userRepository.get()
                        userRepository.update( // clean-up puzzle IDs as they might be stale
                            currentUser.copy(
                                failedPuzzles = emptyList(),
                                ratings = currentUser.ratings.copy(
                                    puzzleStreak = currentUser.ratings.puzzleStreak.copy(
                                        lastPuzzleId = null,
                                    )
                                )
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Database integrity check failed")
        }
        emit(Unit)
    }.flatMapLatest {
        appSettingsRepository.appSettings
    }
}
