package com.paulcraciunas.screens.tools.importgame.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
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
                    Spacer(modifier = Modifier.height(12.dp))
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
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.error,
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
