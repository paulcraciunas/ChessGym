package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

enum class ChipTone { Soft, Accent, Solved, Earned, Locked }

/** Small rounded label-chip. Defaults to "Soft" tone (primary tint). */
@Composable
fun ChessGymChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.Soft,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .background(color = tone.background(), shape = Design.shapes.circle)
            .circleBorder(color = tone.border())
            .padding(horizontal = Design.dimensions.spacing.md, vertical = Design.dimensions.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = tone.foreground(),
                modifier = Modifier.size(Design.dimensions.sizes.iconSmall)
            )
        }
        Text(
            text = text,
            color = tone.foreground(),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun ChipTone.background(): Color = when (this) {
    ChipTone.Soft -> Design.colors.primarySoft
    ChipTone.Accent -> Design.colors.accentSoft
    ChipTone.Solved -> Design.colors.chipSolvedBg
    ChipTone.Earned -> Design.colors.accent
    ChipTone.Locked -> Design.colors.bgTint
}

@Composable
private fun ChipTone.foreground(): Color = when (this) {
    ChipTone.Soft -> Design.colors.primary
    ChipTone.Accent -> Design.colors.chipAccentInk
    ChipTone.Solved -> Design.colors.chipSolvedInk
    ChipTone.Earned -> Design.colors.onPrimary
    ChipTone.Locked -> Design.colors.inkMuted
}

@Composable
private fun ChipTone.border(): Color? = when (this) {
    ChipTone.Solved -> Design.colors.chipSolvedBorder
    ChipTone.Locked -> Design.colors.border
    else -> null
}

@Preview(showBackground = true, name = "Chip Tones")
@Preview(showBackground = true, name = "Chip Tones - Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ChipTonesPreview() {
    ChessGymTheme {
        Column(
            modifier = Modifier.padding(Design.dimensions.spacing.md),
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm)
        ) {
            ChessGymChip(text = "Soft Chip", tone = ChipTone.Soft)
            ChessGymChip(text = "Accent Chip", tone = ChipTone.Accent)
            ChessGymChip(
                text = "Solved Chip",
                tone = ChipTone.Solved,
                leadingIcon = Icons.Default.Check
            )
            ChessGymChip(
                text = "Earned Chip",
                tone = ChipTone.Earned,
                leadingIcon = Icons.Default.Star
            )
            ChessGymChip(
                text = "Locked Chip",
                tone = ChipTone.Locked,
                leadingIcon = Icons.Default.Lock
            )
        }
    }
}
