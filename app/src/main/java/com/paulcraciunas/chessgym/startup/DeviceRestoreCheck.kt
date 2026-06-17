package com.paulcraciunas.chessgym.startup

import android.content.Context
import com.paulcraciunas.puzzles.di.DbName
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DeviceRestoreCheck @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val userRepository: UserRepository,
    @param:ApplicationContext private val context: Context,
    @param:DbName private val databaseName: String,
) : IDeviceRestoreCheck{
    override suspend operator fun invoke() {
        try {
            val settings = appSettingsRepository.appSettings.first()
            if (settings.puzzlesDownloaded) {
                val databaseFile = context.getDatabasePath(databaseName)
                if (!databaseFile.exists()) {
                    // A device restore happened. The local DB asset was excluded,
                    // but the old DataStore flags and user state were restored.
                    Timber.w("Puzzle database file missing. Resetting states.")

                    appSettingsRepository.updatePuzzlesDownloaded(false)
                    val currentUser = userRepository.get()
                    userRepository.update(
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
        } catch (e: Exception) {
            Timber.e(e, "Database integrity check failed")
        }
    }
}
