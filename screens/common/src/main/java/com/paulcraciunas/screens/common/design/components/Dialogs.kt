package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

/**
 * Standard ChessGym dialog shell, backed by [AlertDialog].
 *
 * The [title] lambda is scoped to [DialogTitleScope], which exposes three explicit
 * title variants: [DialogTitleScope.EyebrowTitle], [DialogTitleScope.IconTitle],
 * and [DialogTitleScope.SimpleTitle].
 *
 * The [body] lambda is scoped to [DialogBodyScope], which offers predefined composables
 * for common body layouts (Summary, Message, etc.).
 *
 * Buttons are split into [confirmButton] (required, scoped to [DialogButtonScope]) and
 * [dismissButton] (optional, scoped to [DialogDismissScope]) to preserve AlertDialog's
 * built-in accessibility and overflow handling.
 */
@Composable
fun ChessGymDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable DialogButtonScope.() -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable DialogTitleScope.() -> Unit),
    dismissButton: (@Composable DialogDismissScope.() -> Unit)? = null,
    body: (@Composable DialogBodyScope.() -> Unit) = {},
) {
    val titleScope = remember { DialogTitleScope() }
    val bodyScope = remember { DialogBodyScope() }
    val confirmScope = remember { DialogButtonScope() }
    val dismissScope = remember { DialogDismissScope() }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { titleScope.title() },
        text = { bodyScope.body() },
        confirmButton = { confirmScope.confirmButton() },
        dismissButton = if (dismissButton != null) {
            { dismissScope.dismissButton() }
        } else {
            null
        },
    )
}

@Stable
class DialogTitleScope internal constructor() {
    @Composable
    fun EyebrowTitle(
        eyebrow: String,
        title: String,
        modifier: Modifier = Modifier,
    ) {
        DialogHeader(
            eyebrow = eyebrow,
            title = title,
            modifier = modifier,
        )
    }

    @Composable
    fun IconTitle(
        title: String,
        icon: ImageVector,
        modifier: Modifier = Modifier,
    ) {
        DialogHeader(
            title = title,
            icon = icon,
            modifier = modifier,
        )
    }

    @Composable
    fun SimpleTitle(
        title: String,
        modifier: Modifier = Modifier,
    ) {
        DialogHeader(
            title = title,
            modifier = modifier,
        )
    }
}

/** Scoped receiver for [ChessGymDialog] confirm-button slot. */
@Stable
class DialogButtonScope internal constructor() {
    /** Full-width primary CTA — for single-action summary/info dialogs. */
    @Composable
    fun Primary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        PrimaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
        )
    }

    /** Text button — for confirm/dismiss pairs. */
    @Composable
    fun Standard(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        isDestructive: Boolean = false,
    ) {
        TextButton(onClick = onClick, modifier = modifier) {
            Text(
                text = text,
                color = if (isDestructive) Design.colors.danger else Design.colors.primary,
            )
        }
    }
}

/** Scoped receiver for [ChessGymDialog] dismiss-button slot. */
@Stable
class DialogDismissScope internal constructor() {
    /** Text button — for dismiss actions. */
    @Composable
    fun Standard(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        TextButton(onClick = onClick, modifier = modifier) {
            Text(
                text = text,
                color = Design.colors.primary,
            )
        }
    }
}

/**
 * Scoped receiver for [ChessGymDialog] body content.
 * Provides predefined composable blocks for consistent dialog layouts.
 */
@Stable
class DialogBodyScope internal constructor() {
    /** Large centered numeral + subtitle — for summary dialogs (Rush, Streak, etc.). */
    @Composable
    fun Summary(
        value: String,
        subtitle: String,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = Design.textStyles.displayNumericLarge,
                color = Design.colors.primary,
            )
            ChessGymSpacer(size = SpacerSize.SMALL)
            Text(
                text = subtitle,
                style = Design.textStyles.title,
                color = Design.colors.inkMuted,
            )
        }
    }

    /** Accent chip with star icon — for new high-score states. */
    @Composable
    fun HighScoreBadge(text: String) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChessGymSpacer(size = SpacerSize.MEDIUM)
            ChessGymChip(
                text = text,
                tone = ChipTone.Accent,
                style = ChipStyle.Large,
                leadingIcon = Icons.Default.Star,
            )
        }
    }

    /** Plain body text — for confirmation/info dialogs. */
    @Composable
    fun Message(text: String) {
        Text(
            text = text,
            style = Design.typography.bodyLarge,
            color = Design.colors.inkSoft,
        )
    }

    /** Warning message with secondary explanation — for destructive confirmations. */
    @Composable
    fun WarningMessage(
        text: String,
        explanation: String,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm)) {
            Text(
                text = text,
                style = Design.textStyles.title,
                color = Design.colors.inkSoft,
            )
            Text(
                text = explanation,
                style = Design.typography.bodyLarge,
                color = Design.colors.inkMuted,
            )
        }
    }

    /** Centered body text — for completion/info messages below a Summary. */
    @Composable
    fun CenteredMessage(text: String) {
        ChessGymSpacer(size = SpacerSize.DEFAULT)
        Text(
            text = text,
            style = Design.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Design.colors.inkSoft,
        )
    }

    /** Free-form slot for truly unique content (e.g. text fields, custom layouts). */
    @Composable
    fun Custom(content: @Composable () -> Unit) {
        content()
    }
}

@Composable
private fun DialogHeader(
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    title: String? = null,
    icon: ImageVector? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Design.colors.danger,
                modifier = Modifier.size(Design.dimensions.spacing.section),
            )
            ChessGymSpacer(size = SpacerSize.LARGE)
        }
        if (eyebrow != null) {
            Eyebrow(text = eyebrow)
            ChessGymSpacer(size = SpacerSize.SMALL)
        }
        if (title != null) {
            Text(
                text = title,
                style = Design.typography.headlineLarge,
                color = Design.colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ChessGymDialogPreviewContent(
    confirmButton: @Composable DialogButtonScope.() -> Unit,
    title: (@Composable DialogTitleScope.() -> Unit)? = null,
    dismissButton: (@Composable DialogDismissScope.() -> Unit)? = null,
    body: (@Composable DialogBodyScope.() -> Unit)? = null,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
        ) {
            if (title != null) {
                with(DialogTitleScope()) { title() }
            }
            if (body != null) {
                with(DialogBodyScope()) { body() }
                ChessGymSpacer(size = SpacerSize.HUGE)
            }
            if (dismissButton != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    with(DialogDismissScope()) { dismissButton() }
                    with(DialogButtonScope()) { confirmButton() }
                }
            } else {
                with(DialogButtonScope()) { confirmButton() }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SummaryDialogPreview() {
    ChessGymTheme {
        ChessGymDialogPreviewContent(
            title = { EyebrowTitle(eyebrow = "Puzzle Rush", title = "Run complete") },
            confirmButton = { Primary(text = "Continue", onClick = {}) },
        ) {
            Summary(value = "12", subtitle = "Puzzles Solved")
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SummaryHighScoreDialogPreview() {
    ChessGymTheme {
        ChessGymDialogPreviewContent(
            title = { EyebrowTitle(eyebrow = "Puzzle Rush", title = "Run complete") },
            confirmButton = { Primary(text = "Continue", onClick = {}) },
        ) {
            Summary(value = "19", subtitle = "Puzzles Solved")
            HighScoreBadge(text = "New high score")
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ConfirmationDialogPreview() {
    ChessGymTheme {
        ChessGymDialogPreviewContent(
            title = { SimpleTitle(title = "Sign Out") },
            confirmButton = { Standard(text = "OK", onClick = {}) },
            dismissButton = { Standard(text = "Cancel", onClick = {}) },
        ) {
            Message(text = "Are you sure you want to sign out? You can sign back in at any time.")
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DestructiveDialogPreview() {
    ChessGymTheme {
        ChessGymDialogPreviewContent(
            title = { IconTitle(title = "Delete Account", icon = Icons.Default.Warning) },
            confirmButton = { Standard(text = "Delete", onClick = {}, isDestructive = true) },
            dismissButton = { Standard(text = "Cancel", onClick = {}) },
        ) {
            WarningMessage(
                text = "This will permanently delete your account and all associated data.",
                explanation = "Your puzzles, ratings, and achievements will be lost forever.",
            )
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CompletionDialogPreview() {
    ChessGymTheme {
        ChessGymDialogPreviewContent(
            title = { EyebrowTitle(eyebrow = "All Done!", title = "Failed Puzzles") },
            confirmButton = { Primary(text = "Continue", onClick = {}) },
        ) {
            Summary(value = "8", subtitle = "Puzzles Solved")
            CenteredMessage(text = "Great work! You solved all the puzzles you previously got wrong.")
        }
    }
}
