package com.paulcraciunas.screens.common.achievements

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
fun Achievement.Tier?.tierColor(): Color = when (this) {
    Achievement.Tier.ONE -> Design.colors.achievementTierBronze
    Achievement.Tier.TWO -> Design.colors.achievementTierSilver
    Achievement.Tier.THREE -> Design.colors.achievementTierGold
    Achievement.Tier.FOUR -> Design.colors.achievementTierEmerald
    Achievement.Tier.FIVE -> Design.colors.achievementTierDiamond
    null -> Design.colors.inkSubtle
}
