package com.paulcraciunas.settings.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.userStats: DataStore<Preferences> by preferencesDataStore(name = "user_stats")

internal class DataStoreUserStatsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : UserStatsRepository {
    private val dataStore = context.userStats

    override val userStats: Flow<UserStats> = dataStore.data.map { preferences ->
        UserStats(
            puzzlesPlayed = preferences[PUZZLES_PLAYED] ?: 0,
            puzzlesSolved = preferences[PUZZLES_SOLVED] ?: 0,
            currentRating = preferences[CURRENT_RATING] ?: 1200,
            bestRating = preferences[BEST_RATING] ?: 1200,
            bestPuzzleRushScore = preferences[BEST_PUZZLE_RUSH_SCORE] ?: 0,
            bestBlindModeScore = preferences[BEST_BLIND_MODE_SCORE] ?: 0,
            bestVisualizationScore = preferences[BEST_VISUALIZATION_SCORE] ?: 0
        )
    }

    override suspend fun updatePuzzlesPlayed(count: Int) {
        dataStore.edit { preferences ->
            preferences[PUZZLES_PLAYED] = count
        }
    }

    override suspend fun updatePuzzlesSolved(count: Int) {
        dataStore.edit { preferences ->
            preferences[PUZZLES_SOLVED] = count
        }
    }

    override suspend fun updateCurrentRating(rating: Int) {
        dataStore.edit { preferences ->
            preferences[CURRENT_RATING] = rating
            // Update best rating if current rating is higher
            val currentBest = preferences[BEST_RATING] ?: 1200
            if (rating > currentBest) {
                preferences[BEST_RATING] = rating
            }
        }
    }

    override suspend fun updateBestPuzzleRushScore(score: Int) {
        dataStore.edit { preferences ->
            val currentBest = preferences[BEST_PUZZLE_RUSH_SCORE] ?: 0
            if (score > currentBest) {
                preferences[BEST_PUZZLE_RUSH_SCORE] = score
            }
        }
    }

    override suspend fun updateBestBlindModeScore(score: Int) {
        dataStore.edit { preferences ->
            val currentBest = preferences[BEST_BLIND_MODE_SCORE] ?: 0
            if (score > currentBest) {
                preferences[BEST_BLIND_MODE_SCORE] = score
            }
        }
    }

    override suspend fun updateBestVisualizationScore(score: Int) {
        dataStore.edit { preferences ->
            val currentBest = preferences[BEST_VISUALIZATION_SCORE] ?: 0
            if (score > currentBest) {
                preferences[BEST_VISUALIZATION_SCORE] = score
            }
        }
    }

    companion object {
        private val PUZZLES_PLAYED = intPreferencesKey("puzzles_played")
        private val PUZZLES_SOLVED = intPreferencesKey("puzzles_solved")
        private val CURRENT_RATING = intPreferencesKey("current_rating")
        private val BEST_RATING = intPreferencesKey("best_rating")
        private val BEST_PUZZLE_RUSH_SCORE = intPreferencesKey("best_puzzle_rush_score")
        private val BEST_BLIND_MODE_SCORE = intPreferencesKey("best_blind_mode_score")
        private val BEST_VISUALIZATION_SCORE = intPreferencesKey("best_visualization_score")
    }
}