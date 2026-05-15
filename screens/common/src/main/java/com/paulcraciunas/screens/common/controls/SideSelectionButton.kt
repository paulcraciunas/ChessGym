package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

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
        Design.colors.primary
    } else {
        Design.colors.border.copy(alpha = 0.3f)
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(Design.dimensions.sizes.avatar)
            .clip(Design.shapes.circle)
            .background(if (side == Side.BLACK) Color.White else Color.Black)
            .border(
                width = 4.dp,
                color = borderColor,
                shape = Design.shapes.circle
            )
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize(Design.dimensions.scales.pieceDefault)
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
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
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
