package com.paulcraciunas.screens.home.ui

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
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.home.vm.HomeUiState

@Composable
internal fun HighScoresCard(
    title: String,
    stats: HomeUiState.Stats,
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
                label = stringResource(R.string.user_stat_high_score_puzzles_rush),
                value = stats.bestPuzzleRushScore.toString()
            )
            StatRow(
                label = stringResource(R.string.user_stat_high_score_blind_mode),
                value = stats.bestBlindModeScore.toString()
            )
            StatRow(
                label = stringResource(R.string.user_stat_high_score_board_visualisation),
                value = stats.bestVisualizationScore.toString()
            )
        }
    }
}
