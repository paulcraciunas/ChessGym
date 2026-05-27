package com.paulcraciunas.screens.achievements.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState
import com.paulcraciunas.screens.common.design.components.ProgressRing
import com.paulcraciunas.screens.common.design.components.Title
import com.paulcraciunas.screens.common.design.components.borderSoft
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
internal fun TrophyCaseHero(
    summary: AchievementsUiState.TrophyCaseSummary,
    modifier: Modifier = Modifier,
) {
    val progress = remember(summary.totalEarned, summary.totalAchievements) {
        if (summary.totalAchievements > 0) {
            (summary.totalEarned.toFloat() / summary.totalAchievements).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    val remaining = remember(summary.totalAchievements, summary.totalEarned) {
        summary.totalAchievements - summary.totalEarned
    }
    val totalEarnedText = summary.totalEarned.toString()
    val earnedTotalLabel = stringResource(R.string.achievement_earned_total, summary.totalAchievements)
    val titleLabel = stringResource(R.string.achievement_trophy_case_title)

    val keepGoingLabel = stringResource(R.string.achievement_trophy_case_keep_going, remaining)
    val allEarnedLabel = stringResource(R.string.achievement_trophy_case_all_earned)
    val motivationalText = remember(remaining, keepGoingLabel, allEarnedLabel) {
        if (remaining > 0) keepGoingLabel else allEarnedLabel
    }
    val summaryText = stringResource(
        R.string.achievement_trophy_case_summary,
        summary.inProgress,
        summary.locked,
    )

    Row(
        modifier = modifier
            .shadow(elevation = Design.dimensions.elevation.md, shape = Design.shapes.card)
            .background(Design.colors.bg, Design.shapes.card)
            .border(borderSoft())
            .padding(Design.dimensions.spacing.xxxl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxl),
    ) {
        ProgressRing(progress = progress) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalEarnedText,
                    style = Design.typography.headlineMedium,
                    color = Design.colors.ink,
                )
                Text(
                    text = earnedTotalLabel,
                    style = Design.textStyles.monoSmall,
                    color = Design.colors.inkMuted,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs)
        ) {
            Title(text = titleLabel)
            Text(
                text = motivationalText,
                style = Design.typography.displaySmall,
                color = Design.colors.ink,
            )
            Text(
                text = summaryText,
                style = Design.typography.bodySmall,
                color = Design.colors.inkSoft,
            )
        }
    }
}
