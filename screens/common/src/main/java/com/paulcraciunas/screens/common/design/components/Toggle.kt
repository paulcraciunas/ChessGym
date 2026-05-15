package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
            Switch(
                checked = on,
                onCheckedChange = onChange
            )
        }
        if (!last) {
            HairlineDivider(modifier = Modifier.fillMaxWidth())
        }
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

