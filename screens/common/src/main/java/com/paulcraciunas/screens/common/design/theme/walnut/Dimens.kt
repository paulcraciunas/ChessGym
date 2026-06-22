package com.paulcraciunas.screens.common.design.theme.walnut

import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.ChessGymDimensions

internal val WalnutDimensions = ChessGymDimensions(
    /** Spacing scale (4-pt base, biased to 8-pt rhythm). */
    spacing = ChessGymDimensions.Spacing(
        none = 0.dp,
        xxs = 2.dp,
        xs = 4.dp,
        s = 6.dp,
        sm = 8.dp,
        md = 10.dp,
        lg = 12.dp,
        xl = 14.dp,
        xxl = 16.dp,
        xxxl = 18.dp,
        gut = 20.dp,
        xgut = 24.dp,
        section = 32.dp,
        xsection = 48.dp,
    ),
    elevation = ChessGymDimensions.Elevation(
        none = 0.dp,
        sm = 1.dp,
        md = 4.dp,
        nav = 8.dp,
        lg = 12.dp,
    ),
    /** Minimum interactive sizes — Material requires 48dp. */
    sizes = ChessGymDimensions.Sizes(
        primaryButton = 48.dp,
        primaryButtonIcon = 20.dp,
        pillButton = 44.dp,
        pillButtonIcon = 20.dp,
        outlineButton = 40.dp,
        hitTarget = 48.dp,
        iconButton = 38.dp,
        iconLarge = 32.dp,
        icon = 20.dp,
        iconSmall = 12.dp,
        avatar = 56.dp,
        achievementIcon = 170.dp,
        navBarHeight = 64.dp,
        navBarIconHeight = 24.dp,
        appBarHeight = 64.dp,
        toggleWidth = 42.dp,
        toggleHeight = 24.dp,
        toggleContent = 20.dp,
        progressBar = 6.dp,
        bulletPoint = 4.dp,
        timeControl = 80.dp,
        progressRing = 80.dp,
        trophyTile = 138.dp,
        chessBoardBorder = 14.dp,
        evaluationBar = 30.dp,
        timerChipWidth = 72.dp,
        moveHistoryNumber = 30.dp,
    ),
    blur = ChessGymDimensions.Blur(
        default = 0.dp,
        standard = 16.dp,
    ),
    scales = ChessGymDimensions.Scales(
        piecePawn = 0.65f,
        pieceDefault = 0.8f,
    )
)
