package com.paulcraciunas.settings.user

import android.content.Context
import androidx.core.content.edit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

// TODO Paul: update to use dataStore
@Singleton
class UserSettings @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var puzzlesDownloaded: Boolean
        get() = prefs.getBoolean("puzzles_downloaded", false)
        set(value) = prefs.edit { putBoolean("puzzles_downloaded", value) }

    var playedWeek: Int
        get() = prefs.getInt("played_week", 0)
        set(value) = prefs.edit { putInt("played_week", value) }

    var playedMonth: Int
        get() = prefs.getInt("played_month", 0)
        set(value) = prefs.edit { putInt("played_month", value) }

    var successRate: Float
        get() = prefs.getFloat("success_rate", 0f)
        set(value) = prefs.edit { putFloat("success_rate", value) }

    var rating: Int
        get() = prefs.getInt("rating", 1200)
        set(value) = prefs.edit { putInt("rating", value) }

    var highScore: Int
        get() = prefs.getInt("high_score", 0)
        set(value) = prefs.edit { putInt("high_score", value) }
}
