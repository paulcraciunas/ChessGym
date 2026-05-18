package com.paulcraciunas.screens.achievements.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.common.achievements.description
import com.paulcraciunas.screens.common.achievements.displayName
import com.paulcraciunas.screens.common.achievements.tierColor
import com.paulcraciunas.screens.common.achievements.tierName
import com.paulcraciunas.screens.common.design.components.AchievementMedallion
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.components.LinearProgress
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
fun AchievementDetailDialog(
    achievementState: AchievementState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { SimpleTitle(title = achievementState.achievement.displayName()) },
        body = { Custom { DialogContent(achievementState = achievementState) } },
        buttons = {
            Primary(
                text = stringResource(R.string.generic_close),
                onClick = onDismiss,
            )
        },
    )
}

@Composable
private fun DialogContent(
    achievementState: AchievementState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val itemWidth = Design.dimensions.sizes.medallionLg
    val focusIndex = computeFocusIndex(achievementState).coerceAtLeast(0)
    val listState = rememberLazyListState()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val viewportWidth = maxWidth
        val centerOffsetPx = with(density) {
            val totalOffset = (viewportWidth - itemWidth) / 2
            -totalOffset.toPx().toInt() // Negative because we want the item to move 'right' from the start
        }
        LaunchedEffect(focusIndex) {
            listState.scrollToItem(index = focusIndex, scrollOffset = centerOffsetPx)
        }
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = achievementState.achievement.description(),
                style = Design.typography.bodyMedium,
                color = Design.colors.inkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            ChessGymSpacer(size = SpacerSize.LARGE)
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Design.dimensions.sizes.medallionLg * 1.5f), // Fixed height so LazyRow doesn't jump when scrolling
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = Design.dimensions.spacing.xgut,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                itemsIndexed(fullTierList) { index, tier ->
                    if (tier != null) {
                        TierBadge(
                            tier = tier,
                            isEarned = isTierEarned(achievementState, tier),
                            isFocused = index == focusIndex,
                        )
                    } else {
                        Box(modifier = Modifier.size(Design.dimensions.sizes.medallionContainer)) {} // placeholder
                    }
                }
            }
            FocusTierDetail(achievementState = achievementState)
        }
    }
}

@Composable
private fun TierBadge(
    tier: Achievement.Tier,
    isEarned: Boolean,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    val scale = Design.dimensions.scales.achievementDetail(isFocused)
    Column(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AchievementMedallion(
            hue = if (isEarned) tier.tierColor() else Design.colors.inkSubtle,
            size = Design.dimensions.sizes.medallionLg,
            earned = isEarned,
            locked = !isEarned,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.military_medal_icon),
                    contentDescription = null,
                    modifier = Modifier.size(Design.dimensions.sizes.medallionLg * 0.5f),
                    tint = if (isEarned) Design.colors.primary else Design.colors.inkMuted,
                )
            },
        )
        ChessGymSpacer(size = SpacerSize.SMALL)
    }
}

@Composable
private fun FocusTierDetail(
    achievementState: AchievementState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = achievementState.achievement.tierName(achievementState.displayTier()),
            style = Design.typography.bodyMedium,
            color = Design.colors.accent,
            textAlign = TextAlign.Center,
        )
        ChessGymSpacer(size = SpacerSize.DEFAULT)
        if (achievementState !is AchievementState.Unearned) {
            Eyebrow(text = stringResource(R.string.achievement_earned_label))
            ChessGymSpacer(size = SpacerSize.DEFAULT)
        }
        Text(
            text = stringResource(
                R.string.achievement_detail_progress,
                achievementState.currentProgress,
                if (achievementState is AchievementState.Complete) {
                    achievementState.currentProgress
                } else {
                    (achievementState as AchievementState.Incomplete).nextThreshold
                },
            ),
            style = Design.textStyles.monoSmall,
            color = Design.colors.inkMuted,
        )
        ChessGymSpacer(size = SpacerSize.SMALL)
        LinearProgress(
            progress = achievementState.progress(),
            color = achievementState.currentTier.tierColor(),
        )
    }
}

private fun computeFocusIndex(state: AchievementState): Int = when (state) {
    is AchievementState.Unearned -> fullTierList.indexOf(Achievement.Tier.ONE)
    is AchievementState.Earned -> fullTierList.indexOf(state.currentTier)
    is AchievementState.Complete -> fullTierList.indexOf(Achievement.Tier.FIVE)
}

private fun isTierEarned(state: AchievementState, tier: Achievement.Tier): Boolean {
    val currentTier = state.currentTier ?: return false
    return tier <= currentTier
}

private val fullTierList: List<Achievement.Tier?> = buildList {
    add(null) // Pad a null at the beginning, so there's scroll space
    addAll(Achievement.Tier.entries)
    add(null) // Pad a null at the end, so there's scroll space
}
