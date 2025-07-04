package com.paulcraciunas.chessgym.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paulcraciunas.settings.user.UserStats
import com.paulcraciunas.global.resources.R

@Composable
internal fun UserStatsCard(
    title: String,
    stats: UserStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            StatRow(
                label = stringResource(R.string.user_stat_puzzles_played),
                value = stats.puzzlesPlayed.toString()
            )
            StatRow(
                label = stringResource(R.string.user_stat_puzzles_solved),
                value = stats.puzzlesSolved.toString()
            )
            StatRow(
                label = stringResource(R.string.user_stat_puzzles_success),
                value = if (stats.puzzlesPlayed > 0) {
                    "${(stats.puzzlesSolved * 100 / stats.puzzlesPlayed)}%"
                } else {
                    "0%"
                }
            )
            StatRow(
                label = stringResource(R.string.user_stat_current_rating),
                value = stats.currentRating.toString()
            )
            StatRow(
                label = stringResource(R.string.user_stat_high_score_rated_puzzle),
                value = stats.bestRating.toString()
            )
        }
    }
}
