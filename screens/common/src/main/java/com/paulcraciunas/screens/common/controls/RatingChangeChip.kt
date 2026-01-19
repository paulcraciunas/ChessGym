package com.paulcraciunas.screens.common.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.theme.LoadingColors
import com.paulcraciunas.screens.common.theme.LoadingTheme

@Composable
fun RatingChangeChip(
    ratingChange: Int,
    modifier: Modifier = Modifier
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (isPositive) "+$ratingChange" else ratingChange.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}
