package com.paulcraciunas.chessgym.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.achievements.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForceAchievementScreen(
    uiState: ForceAchievementUiState,
    onNavigateBack: () -> Unit,
    onForceAchievement: (Achievement, Achievement.Tier) -> Unit,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = "Debug: Force Achievement",
                navButton = { Back(onClick = onNavigateBack) },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = uiState.achievements,
                key = { it.achievement.name },
            ) { item ->
                AchievementItemCard(
                    item = item,
                    onTierSelected = { tier ->
                        onForceAchievement(item.achievement, tier)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AchievementItemCard(
    item: ForceAchievementUiState.AchievementItem,
    onTierSelected: (Achievement.Tier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = item.achievement.displayName(),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "Current: ${item.currentTier?.let { "Tier ${it.number}" } ?: "None"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Achievement.Tier.entries.forEach { tier ->
                    FilterChip(
                        selected = item.currentTier == tier,
                        onClick = { onTierSelected(tier) },
                        label = { Text("Tier ${tier.number}") },
                    )
                }
            }
        }
    }
}
