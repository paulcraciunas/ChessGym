package com.paulcraciunas.screens.achievements.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.TrophyCaseSummary
import com.paulcraciunas.screens.common.design.components.ChessGymHeroCard
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.ProgressRing
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.Title
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
internal fun TrophyCaseHero(
    summary: TrophyCaseSummary,
    modifier: Modifier = Modifier,
) {
    val remaining = summary.totalAchievements - summary.totalEarned
    val progress = if (summary.totalAchievements > 0) {
        summary.totalEarned.toFloat() / summary.totalAchievements
    } else {
        0f
    }

    ChessGymHeroCard(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxl),
        ) {
            ProgressRing(progress = progress) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.totalEarned.toString(),
                        style = Design.typography.headlineMedium,
                        color = Design.colors.ink,
                    )
                    Text(
                        text = stringResource(R.string.achievement_earned_total, summary.totalAchievements),
                        style = Design.textStyles.monoSmall,
                        color = Design.colors.inkMuted,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Title(text = stringResource(R.string.achievement_trophy_case_title))
                ChessGymSpacer(size = SpacerSize.SMALL)
                Text(
                    text = if (remaining > 0) {
                        stringResource(R.string.achievement_trophy_case_keep_going, remaining)
                    } else {
                        stringResource(R.string.achievement_trophy_case_all_earned)
                    },
                    style = Design.typography.displaySmall,
                    color = Design.colors.ink,
                )
                ChessGymSpacer(size = SpacerSize.SMALL)
                Text(
                    text = stringResource(
                        R.string.achievement_trophy_case_summary,
                        summary.inProgress,
                        summary.locked,
                    ),
                    style = Design.typography.bodySmall,
                    color = Design.colors.inkSoft,
                )
            }
        }
    }
}
