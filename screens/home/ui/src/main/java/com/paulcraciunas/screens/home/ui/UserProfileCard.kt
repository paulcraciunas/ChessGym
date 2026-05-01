package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.achievements.tierName
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun UserProfileCard(
    userProfile: HomeUiState.UserProfile,
    ribbons: List<HomeUiState.Ribbon>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .testTag { HomeScreenTags.Profile.CARD }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main row: Avatar, Name, Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.initials(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag { HomeScreenTags.Profile.INITIALS }
                    )
                }

                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .testTag { HomeScreenTags.Profile.NAME }
                )

                Text(
                    text = userProfile.currentRating.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag { HomeScreenTags.Profile.RATING }
                )
            }

            // Stats Rows
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatRow(
                    label = "Activities",
                    value = userProfile.totalActivities.toString(),
                    modifier = Modifier.testTag { HomeScreenTags.Profile.ACTIVITIES }
                )
                StatRow(
                    label = "Member Since",
                    value = userProfile.joinDate.format(formatter)
                )
            }

            if (ribbons.isNotEmpty()) {
                RibbonRow(ribbons = ribbons)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RibbonRow(
    ribbons: List<HomeUiState.Ribbon>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ribbons.forEach { ribbon ->
            Icon(
                painter = painterResource(id = R.drawable.military_medal_icon),
                contentDescription = ribbon.achievement.tierName(ribbon.tier),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private val formatter = DateTimeFormatter.ofPattern("MMM yyyy")

@Preview("UserProfileCard")
@Preview("UserProfileCard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun UserProfileCardPreview() {
    ChessGymTheme {
        UserProfileCard(
            userProfile = HomeUiState.UserProfile(
                name = "John Doe",
                currentRating = 1547,
                totalActivities = 142,
                joinDate = LocalDate.of(2024, 3, 15)
            ),
            ribbons = listOf(
                HomeUiState.Ribbon(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.THREE),
                HomeUiState.Ribbon(Achievement.PUZZLE_RUSH_SESSIONS, Achievement.Tier.ONE),
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
