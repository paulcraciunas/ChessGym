package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
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
 * Buttons are applied via [buttons], (scoped to [DialogButtonScope]).
 */
@Composable
fun ChessGymDialog(
    onDismissRequest: () -> Unit,
    buttons: @Composable DialogButtonScope.() -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable DialogTitleScope.() -> Unit),
    body: (@Composable DialogBodyScope.() -> Unit) = {},
) {
    val titleScope = remember { DialogTitleScope() }
    val bodyScope = remember { DialogBodyScope() }
    val buttonsScope = remember { DialogButtonScope() }

    AlertDialog(
        containerColor = Design.colors.surface,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { titleScope.title() },
        text = { bodyScope.body() },
        confirmButton = { buttonsScope.buttons() },
        dismissButton = null
    )
}

enum class DialogIconTone { Accent, Danger }

@Stable
class DialogTitleScope internal constructor() {
    @Composable
    fun EyebrowTitle(
        eyebrow: String,
        title: String,
        modifier: Modifier = Modifier,
    ) {
        DialogHeader(eyebrow = eyebrow, title = title, modifier = modifier)
    }

    @Composable
    fun IconTitle(
        title: String,
        icon: ImageVector,
        modifier: Modifier = Modifier,
        tone: DialogIconTone = DialogIconTone.Danger,
    ) {
        DialogHeader(title = title, icon = icon, iconTone = tone, modifier = modifier)
    }

    @Composable
    fun SimpleTitle(
        title: String,
        modifier: Modifier = Modifier,
    ) {
        DialogHeader(title = title, modifier = modifier)
    }
}

@Composable
private fun DialogHeader(
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    title: String? = null,
    icon: ImageVector? = null,
    iconTone: DialogIconTone = DialogIconTone.Danger,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            IconBadge(
                imageVector = icon,
                style = IconStyle.Circle,
                borderType = when (iconTone) {
                    DialogIconTone.Accent -> IconBorderType.None
                    DialogIconTone.Danger -> IconBorderType.Hard
                },
                tint = when (iconTone) {
                    DialogIconTone.Accent -> IconTintType.Accent
                    DialogIconTone.Danger -> IconTintType.Danger
                },
            )
            ChessGymSpacer(size = SpacerSize.DEFAULT)
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
            modifier = modifier.testTag { ChessGymDialogTags.CONFIRM }.fillMaxWidth(),
        )
    }

    /**
     * Two equally-weighted buttons — outlined dismiss + filled confirm.
     * Pass this as confirmButton with dismissButton = null, since
     * it renders both buttons itself.
     */
    @Composable
    fun Paired(
        confirmText: String,
        onConfirm: () -> Unit,
        dismissText: String,
        onDismiss: () -> Unit,
        isDestructive: Boolean = false,
        confirmEnabled: Boolean = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.md),
        ) {
            PrimaryButton(
                text = dismissText,
                onClick = onDismiss,
                style = PrimaryButtonStyle.Clear,
                modifier = Modifier.testTag { ChessGymDialogTags.DISMISS }
                    .weight(1f)
                    .height(Design.dimensions.sizes.primaryButton),
            )
            PrimaryButton(
                text = confirmText,
                onClick = onConfirm,
                enabled = confirmEnabled,
                style = if (isDestructive) PrimaryButtonStyle.Danger else PrimaryButtonStyle.Normal,
                modifier = Modifier.testTag { ChessGymDialogTags.CONFIRM }.weight(1f),
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

    /** Centered body text — for confirmation/info dialogs. */
    @Composable
    fun Message(text: String) {
        Text(
            text = text,
            style = Design.typography.bodyLarge,
            color = Design.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
    }

    /** Centered rich body text — for messages with bold/italic spans. */
    @Composable
    fun Message(text: AnnotatedString) {
        Text(
            text = text,
            style = Design.typography.bodyLarge,
            color = Design.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
    }

    /** Danger-tinted callout with bullet list — for destructive consequence warnings. */
    @Composable
    fun Danger(
        title: String,
        items: List<String>,
        modifier: Modifier = Modifier,
    ) {
        ChessGymSpacer(size = SpacerSize.LARGE)
        DangerBlock(
            title = title,
            items = items,
            modifier = modifier,
        )
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

object ChessGymDialogTags {
    const val CONFIRM = "ChessGymDialog_confirm"
    const val DISMISS = "ChessGymDialog_dismiss"
}

@Composable
private fun ChessGymDialogPreviewContent(
    buttons: @Composable DialogButtonScope.() -> Unit,
    title: (@Composable DialogTitleScope.() -> Unit)? = null,
    body: (@Composable DialogBodyScope.() -> Unit)? = null,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = Design.colors.surface,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                with(DialogButtonScope()) { buttons() }
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
            buttons = { Primary(text = "Continue", onClick = {}) },
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
            buttons = { Primary(text = "Continue", onClick = {}) },
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
            title = {
                IconTitle(
                    title = "Sign out?",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    tone = DialogIconTone.Accent,
                )
            },
            buttons = {
                Paired(
                    confirmText = "Sign out",
                    onConfirm = {},
                    dismissText = "Cancel",
                    onDismiss = {},
                )
            },
        ) {
            Message(text = "Are you sure you want to sign out? Your progress is saved — you can sign back in at any time.")
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ConfirmationDisabledDialogPreview() {
    ChessGymTheme {
        ChessGymDialogPreviewContent(
            title = {
                IconTitle(
                    title = "Sign out?",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    tone = DialogIconTone.Accent,
                )
            },
            buttons = {
                Paired(
                    confirmText = "Sign out",
                    onConfirm = {},
                    dismissText = "Cancel",
                    onDismiss = {},
                    confirmEnabled = false,
                )
            },
        ) {
            Message(text = "Are you sure you want to sign out? Your progress is saved — you can sign back in at any time.")
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DestructiveDialogPreview() {
    ChessGymTheme {
        ChessGymDialogPreviewContent(
            title = {
                IconTitle(
                    title = "Delete account?",
                    icon = Icons.Default.Warning,
                    tone = DialogIconTone.Danger,
                )
            },
            buttons = {
                Paired(
                    confirmText = "Delete",
                    onConfirm = {},
                    dismissText = "Cancel",
                    onDismiss = {},
                    isDestructive = true,
                )
            },
        ) {
            Message(text = annotatedTextResource(R.string.delete_account_dialog_body))
            Danger(
                title = "You will lose",
                items = listOf(
                    "Puzzles solved & ratings",
                    "Streaks & achievements",
                    "Saved games & analyses",
                ),
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
            buttons = { Primary(text = "Continue", onClick = {}) },
        ) {
            Summary(value = "8", subtitle = "Puzzles Solved")
            CenteredMessage(text = "Great work! You solved all the puzzles you previously got wrong.")
        }
    }
}
