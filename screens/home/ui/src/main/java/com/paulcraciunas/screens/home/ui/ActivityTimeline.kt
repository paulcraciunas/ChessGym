package com.paulcraciunas.screens.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.components.EyebrowType
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.home.vm.HomeUiState

@Composable
internal fun ActivityGroupItem(
    group: HomeUiState.HistoryGroup,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm)
    ) {
        Eyebrow(text = group.label, type = EyebrowType.Soft)
        group.events.forEach { TimelineEventItem(event = it) }
    }
}

@Composable
internal fun EmptyTimelineContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(vertical = Design.dimensions.spacing.section)
            .testTag { HomeScreenTags.Timeline.EMPTY },
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm)
    ) {
        Text(
            text = stringResource(R.string.home_timeline_empty_title),
            style = Design.typography.titleMedium,
            color = Design.colors.ink,
        )
        Text(
            text = stringResource(R.string.home_timeline_empty_description),
            style = Design.typography.bodyMedium,
            color = Design.colors.inkSoft,
        )
    }
}
