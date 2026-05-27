package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.achievements.tierName
import com.paulcraciunas.screens.common.design.components.ChessGymElevatedCard
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.components.HairlineDivider
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun UserProfileCard(
    userProfile: HomeUiState.UserProfile,
    ribbons: List<HomeUiState.Ribbon>,
    modifier: Modifier = Modifier,
) {
    ChessGymElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag { HomeScreenTags.Profile.CARD },
        contentPadding = PaddingValues(Design.dimensions.spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
    ) {
        NameAndRating(userProfile = userProfile)
        HairlineDivider(modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Property(
                label = stringResource(R.string.home_label_activities),
                value = userProfile.totalActivities.toString(),
                modifier = Modifier.testTag { HomeScreenTags.Profile.ACTIVITIES }
            )
            Property(
                label = stringResource(R.string.home_label_member_since),
                value = userProfile.joinDate.format(formatter)
            )
        }

        if (ribbons.isNotEmpty()) {
            RibbonRow(ribbons = ribbons)
        }
    }
}

@Composable
private fun NameAndRating(
    userProfile: HomeUiState.UserProfile,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(Design.dimensions.sizes.avatar)
                .clip(Design.shapes.circle)
                .background(Design.colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userProfile.initial(),
                style = Design.typography.headlineSmall,
                color = Design.colors.onPrimary,
                modifier = Modifier.testTag { HomeScreenTags.Profile.INITIALS }
            )
        }
        ChessGymSpacer(size = SpacerSize.LARGE)
        Column(modifier = Modifier.weight(1f)) {
            ChessGymSpacer(size = SpacerSize.SMALL)
            Eyebrow(text = stringResource(R.string.home_label_profile))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userProfile.name,
                    style = Design.typography.headlineSmall,
                    color = Design.colors.ink,
                    modifier = Modifier
                        .testTag { HomeScreenTags.Profile.NAME }
                )
                if (userProfile.isSupporter) {
                    ChessGymSpacer(size = SpacerSize.SMALL)
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Supporter",
                        tint = Design.colors.accent,
                        modifier = Modifier.size(Design.dimensions.sizes.icon)
                    )
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            ChessGymSpacer(size = SpacerSize.SMALL)
            Eyebrow(text = stringResource(R.string.home_label_rating))
            Text(
                text = userProfile.currentRating.toString(),
                style = Design.typography.displaySmall,
                color = Design.colors.primary,
                modifier = Modifier.testTag { HomeScreenTags.Profile.RATING }
            )
        }
    }
}

@Composable
internal fun Property(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = Design.typography.bodySmall,
            color = Design.colors.inkMuted,
        )
        Text(
            text = value,
            style = Design.textStyles.title,
            color = Design.colors.ink,
        )
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
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
    ) {
        ribbons.forEach { ribbon ->
            Icon(
                painter = painterResource(id = R.drawable.military_medal_icon),
                contentDescription = ribbon.achievement.tierName(ribbon.tier),
                modifier = Modifier.size(Design.dimensions.sizes.icon),
                tint = Design.colors.accent,
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
                name = "JohnDoe",
                currentRating = 1547,
                totalActivities = 142,
                joinDate = LocalDate.of(2024, 3, 15),
                isSupporter = true,
            ),
            ribbons = listOf(
                HomeUiState.Ribbon(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.THREE),
                HomeUiState.Ribbon(Achievement.PUZZLE_RUSH_SESSIONS, Achievement.Tier.ONE),
            ),
            modifier = Modifier.padding(Design.dimensions.spacing.xxl)
        )
    }
}
