package com.paulcraciunas.screens.common.design.theme.walnut

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.ChessGymRadius
import com.paulcraciunas.screens.common.design.theme.ChessGymShape

internal val WalnutRadii = ChessGymRadius(
    none = 0.dp,
    xxs = 2.dp,
    xs = 6.dp,
    sm = 8.dp,
    md = 10.dp,
    lg = 12.dp,
    xl = 14.dp,
    xxl = 18.dp,
    pill = 999.dp,
)

internal val WalnutShapes = ChessGymShape(
    soft = RoundedCornerShape(WalnutRadii.xxs),
    card = RoundedCornerShape(WalnutRadii.lg),
    cardCompact = RoundedCornerShape(WalnutRadii.sm),
    pill = RoundedCornerShape(WalnutRadii.pill),
    circle = CircleShape,
    navBar = RoundedCornerShape(WalnutRadii.xxl),
    button = RoundedCornerShape(WalnutRadii.lg),
    borderAccent = RoundedCornerShape(
        topStart = WalnutRadii.md,
        bottomStart = WalnutRadii.md,
        topEnd = WalnutRadii.none,
        bottomEnd = WalnutRadii.none
    ),
    buttonOutline = RoundedCornerShape(WalnutRadii.md),
    buttonPill = RoundedCornerShape(WalnutRadii.pill),
)

internal val WalnutStandardShapes = Shapes(
    extraSmall = RoundedCornerShape(WalnutRadii.xs),
    small = RoundedCornerShape(WalnutRadii.sm),
    medium = RoundedCornerShape(WalnutRadii.lg),
    large = RoundedCornerShape(WalnutRadii.xl),
    extraLarge = RoundedCornerShape(WalnutRadii.xxl),
)
