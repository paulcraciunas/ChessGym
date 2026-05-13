package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

/** Settings-style row: title + optional subtitle + trailing toggle/control. */
@Composable
fun ToggleRow(
    title: String,
    on: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    last: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Design.dimensions.spacing.xxl, vertical = Design.dimensions.spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Design.colors.ink,
                    style = MaterialTheme.typography.titleMedium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Design.colors.inkMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = Design.dimensions.spacing.xxs)
                    )
                }
            }
            ChessGymToggle(on = on, onChange = onChange)
        }
        if (!last) {
            HairlineDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

/** A 42×24 toggle that matches the Settings screen design. */
@Composable
private fun ChessGymToggle(
    on: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val track by animateColorAsState(
        targetValue = if (on) Design.colors.primary else Design.colors.bgTint,
        label = "toggle-track",
    )
    val knobX by animateDpAsState(
        targetValue = if (on) Design.dimensions.sizes.toggleContent else Design.dimensions.spacing.xxs,
        label = "toggle-knob",
    )
    Box(
        modifier = modifier
            .size(width = Design.dimensions.sizes.toggleWidth, height = Design.dimensions.sizes.toggleHeight)
            .background(track, Design.shapes.circle)
            .clickable(enabled = enabled) { onChange(!on) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset { IntOffset(x = knobX.roundToPx(), y = 0) }
                .size(Design.dimensions.sizes.toggleContent)
                .background(Color.White, Design.shapes.circle)
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ToggleRowPreview() {
    ChessGymTheme {
        Column {
            ToggleRow(
                title = "Show coordinate labels",
                on = true,
                onChange = {}
            )
            ToggleRow(
                title = "Vibrate on move",
                subtitle = "Vibrate the device when a move is made",
                on = false,
                onChange = {},
                last = true
            )
        }
    }
}

