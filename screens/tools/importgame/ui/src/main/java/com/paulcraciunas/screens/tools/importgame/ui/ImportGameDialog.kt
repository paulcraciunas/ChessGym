package com.paulcraciunas.screens.tools.importgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.tools.importgame.vm.ImportType

@Composable
fun ImportGameDialog(
    type: ImportType,
    error: String?,
    onImport: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }

    val dialogTitle = when (type) {
        ImportType.FEN -> stringResource(R.string.import_dialog_title_fen)
        ImportType.PGN -> stringResource(R.string.import_dialog_title_pgn)
    }
    val hint = when (type) {
        ImportType.FEN -> stringResource(R.string.import_dialog_hint_fen)
        ImportType.PGN -> stringResource(R.string.import_dialog_hint_pgn)
    }

    ChessGymDialog(
        onDismissRequest = onDismiss,
        title = { SimpleTitle(title = dialogTitle) },
        buttons = {
            Paired(
                confirmText = stringResource(R.string.import_dialog_confirm),
                onConfirm = { onImport(text) },
                dismissText = stringResource(R.string.import_dialog_cancel),
                onDismiss = onDismiss,
                confirmEnabled = text.isNotBlank(),
            )
        },
        modifier = modifier.testTag { ImportGameScreenTags.IMPORT_DIALOG },
    ) {
        Custom {
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
    }
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
