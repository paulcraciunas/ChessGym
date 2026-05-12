package com.paulcraciunas.screens.tools.importgame.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.importgame.vm.ImportType

@Composable
internal fun ImportGameDialog(
    type: ImportType,
    error: String?,
    onImport: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }

    val title = when (type) {
        ImportType.FEN -> stringResource(R.string.import_dialog_title_fen)
        ImportType.PGN -> stringResource(R.string.import_dialog_title_pgn)
    }
    val hint = when (type) {
        ImportType.FEN -> stringResource(R.string.import_dialog_hint_fen)
        ImportType.PGN -> stringResource(R.string.import_dialog_hint_pgn)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(
                modifier = Modifier.testTag { ImportGameScreenTags.IMPORT_DIALOG },
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(hint) },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag { ImportGameScreenTags.IMPORT_TEXT_FIELD },
                )

                if (error != null) {
                    ChessGymSpacer(size = SpacerSize.LARGE)
                    ErrorInDialog(
                        message = error,
                        modifier = Modifier.testTag { ImportGameScreenTags.IMPORT_ERROR },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onImport(text) },
                enabled = text.isNotBlank(),
                modifier = Modifier.testTag { ImportGameScreenTags.IMPORT_CONFIRM },
            ) {
                Text(stringResource(R.string.import_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.import_dialog_cancel))
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ErrorInDialog(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Design.shapes.cardCompact)
            .background(Design.colors.danger.copy(alpha = 0.15f))
            .padding(Design.dimensions.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(Design.dimensions.spacing.section)
                .clip(CircleShape)
                .background(Design.colors.danger.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = Design.colors.danger,
                modifier = Modifier.size(Design.dimensions.sizes.icon),
            )
        }
        Text(
            text = message,
            style = Design.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Design.colors.danger,
            textAlign = TextAlign.Start,
        )
    }
}

@Preview("ImportDialog - FEN")
@Preview("ImportDialog - FEN (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ImportDialogFenPreview() {
    ChessGymTheme {
        ImportGameDialog(
            type = ImportType.FEN,
            error = null,
            onImport = {},
            onDismiss = {},
        )
    }
}

@Preview("ImportDialog - PGN with error")
@Composable
private fun ImportDialogPgnErrorPreview() {
    ChessGymTheme {
        ImportGameDialog(
            type = ImportType.PGN,
            error = "Invalid PGN notation. Please check and try again.",
            onImport = {},
            onDismiss = {},
        )
    }
}
