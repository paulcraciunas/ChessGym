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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.common.theme.LoadingTheme

@Composable
fun RatingChangeChip(
    ratingChange: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    val isPositive = ratingChange > 0
    val backgroundColor = if (isPositive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    }
    val contentColor = if (isPositive) {
        LoadingTheme.colors.success
    } else {
        MaterialTheme.colorScheme.error
    }

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )
        CompositionLocalProvider(LocalTextStyle provides textStyle) {
            Text(
                text = if (isPositive) "+$ratingChange" else ratingChange.toString(),
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
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
        RatingChangeChip(
            ratingChange = 25,
            iconSize = 24.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            textStyle = MaterialTheme.typography.titleMedium
        )
    }
}
