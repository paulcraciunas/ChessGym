package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun UserProfileCard(
    userProfile: HomeUiState.UserProfile,
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
            modifier = Modifier.padding(16.dp)
        )
    }
}
