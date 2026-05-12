package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun RatingChangeChip(
    ratingChange: Int,
    modifier: Modifier = Modifier,
) {
    val isPositive = ratingChange > 0
    val backgroundColor = if (isPositive) {
        Design.colors.chipSolvedBg
    } else {
        Design.colors.danger.copy(alpha = 0.15f)
    }
    val contentColor = if (isPositive) {
        Design.colors.success
    } else {
        Design.colors.danger
    }

    Row(
        modifier = modifier
            .clip(Design.shapes.cardCompact)
            .background(backgroundColor)
            .padding(PaddingValues(
                horizontal = Design.dimensions.spacing.sm,
                vertical = Design.dimensions.spacing.xs,
            )),
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(Design.dimensions.sizes.icon)
        )
        Text(
            text = if (isPositive) "+$ratingChange" else ratingChange.toString(),
            style = Design.typography.labelLarge,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun RatingChangeChipPositivePreview() {
    ChessGymTheme {
        RatingChangeChip(ratingChange = 15)
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun RatingChangeChipNegativePreview() {
    ChessGymTheme {
        RatingChangeChip(ratingChange = -12)
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun RatingChangeChipLargePreview() {
    ChessGymTheme {
        RatingChangeChip(ratingChange = 25)
    }
}
