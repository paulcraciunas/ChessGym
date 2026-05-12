package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design

@Stable
@Composable
fun borderSoft(): BorderStroke = BorderStroke(1.dp, Design.colors.borderSoft)

@Stable
@Composable
fun borderPrimary(): BorderStroke = BorderStroke(1.dp, Design.colors.primary)

@Stable
@Composable
fun Modifier.circleBorder(color: Color?): Modifier =
    color?.let {
        border(1.dp, SolidColor(color), Design.shapes.circle)
    } ?: this

@Stable
@Composable
fun Modifier.surfaceBorder(): Modifier =
    border(2.dp, Design.colors.surface, Design.shapes.circle)
