package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.common.theme.GlobalTokens

@Composable
internal fun SideSelectionButton(
    @DrawableRes iconRes: Int,
    side: Side,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (side == Side.BLACK) Color.White else Color.Black)
            .border(
                width = 4.dp,
                color = borderColor,
                shape = CircleShape
            )
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize(GlobalTokens.scaleFactorDefault)
                .aspectRatio(1f)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SideSelectionButtonWhiteSelectedPreview() {
    ChessGymTheme {
        SideSelectionButton(
            iconRes = R.drawable.king_white,
            side = Side.WHITE,
            contentDescription = "Play as white",
            isSelected = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SideSelectionButtonBlackUnselectedPreview() {
    ChessGymTheme {
        SideSelectionButton(
            iconRes = R.drawable.king_black,
            side = Side.BLACK,
            contentDescription = "Play as black",
            isSelected = false,
            onClick = {},
        )
    }
}
