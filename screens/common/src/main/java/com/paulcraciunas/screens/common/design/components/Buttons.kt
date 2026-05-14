package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.paulcraciunas.screens.common.design.theme.Design

/** Solid primary button — for the dominant CTA on a screen. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(Design.dimensions.sizes.primaryButton),
        enabled = enabled,
        shape = Design.shapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = Design.colors.primaryDeep,
            contentColor = Design.colors.onPrimary,
        ),
        contentPadding = PaddingValues(horizontal = Design.dimensions.spacing.gut, vertical = Design.dimensions.spacing.lg),
    ) {
        if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(Design.dimensions.sizes.primaryButtonIcon))
            ChessGymSpacer()
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Pill primary CTA — used on Blind / Find the Square play buttons. */
@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(Design.dimensions.sizes.pillButton),
        enabled = enabled,
        shape = Design.shapes.circle,
        colors = ButtonDefaults.buttonColors(
            containerColor = Design.colors.primary,
            contentColor = Design.colors.onPrimary,
        ),
        contentPadding = PaddingValues(horizontal = Design.dimensions.spacing.xgut, vertical = Design.dimensions.spacing.md),
    ) {
        if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(Design.dimensions.sizes.pillButtonIcon))
            ChessGymSpacer()
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Hairline-bordered secondary button — segmented controls etc. */
@Composable
fun OutlineSegmentButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(Design.dimensions.sizes.outlineButton),
        shape = Design.shapes.buttonOutline,
        border = if (selected) borderPrimary() else borderSoft(),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Design.colors.primarySoft else Design.colors.surface,
            contentColor = if (selected) Design.colors.primary else Design.colors.inkSoft,
        ),
        contentPadding = PaddingValues(horizontal = Design.dimensions.spacing.lg, vertical = Design.dimensions.spacing.sm),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Round icon-only button with a soft primary tint when active. */
@Composable
fun IconCircleButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(Design.dimensions.sizes.iconButton),
        colors = IconButtonDefaults.iconButtonColors(contentColor = Design.colors.ink),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Design.dimensions.sizes.icon)
        )
    }
}
