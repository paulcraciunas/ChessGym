package com.paulcraciunas.screens.achievements.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementCategory
import com.paulcraciunas.screens.common.design.components.SectionHeaderTitle
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun CategoryHeading(
    category: AchievementCategory,
    earnedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    // 1. Pre-calculate dynamic values and strings
    val categoryTitle = stringResource(category.labelRes())
    val countText = stringResource(
        R.string.achievement_earned_count,
        earnedCount,
        totalCount,
    )
    val dividerColor = Design.colors.divider
    val spacingSm = Design.dimensions.spacing.sm

    Column(
        modifier = modifier.padding(bottom = spacingSm),
    ) {
        Row(
            modifier = Modifier
                .padding(top = Design.dimensions.spacing.xl, bottom = spacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeaderTitle(text = categoryTitle)
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacingSm)
                    .drawBehind {
                        val y = size.height / 2f
                        drawLine(
                            color = dividerColor,
                            start = Offset(x = 0f, y = y),
                            end = Offset(x = size.width, y = y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            )
        }
        Text(
            text = countText,
            style = Design.typography.bodySmall,
            color = Design.colors.inkSoft,
        )
    }
}

@StringRes
private fun AchievementCategory.labelRes(): Int = when (this) {
    AchievementCategory.PUZZLES -> R.string.achievement_group_puzzles
    AchievementCategory.RUSH_AND_STREAK -> R.string.achievement_group_rush_and_streak
    AchievementCategory.BOARD_VISION -> R.string.achievement_group_board_vision
    AchievementCategory.SKILL_AND_MASTERY -> R.string.achievement_group_skill_and_mastery
    AchievementCategory.DEDICATION -> R.string.achievement_group_dedication
}

@Preview(name = "Category Heading", showBackground = true)
@Preview(name = "Category Heading (Dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CategoryHeadingPreview() {
    ChessGymTheme {
        CategoryHeading(
            category = AchievementCategory.PUZZLES,
            earnedCount = 3,
            totalCount = 5,
        )
    }
}
