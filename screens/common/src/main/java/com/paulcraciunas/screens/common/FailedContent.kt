package com.paulcraciunas.screens.common

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.controls.RefreshButton
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

private val ErrorBadgeSize = 96.dp
private val ErrorIconSize = 56.dp
private const val ErrorContainerAlpha = 0.35f

@Composable
fun FailedContent(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.generic_error_title),
    message: String = stringResource(R.string.generic_error_try_again),
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Design.colors.primarySoft),
    ) {
        ErrorBody(
            title = title,
            message = message,
            onRetry = onRetry,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        )
        Footer(modifier = Modifier.padding(bottom = 24.dp))
    }
}

@Composable
private fun ErrorBody(
    title: String,
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ErrorBadge()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        onRetry?.let { retry ->
            Spacer(modifier = Modifier.height(24.dp))
            RefreshButton(onClick = retry, text = R.string.generic_retry)
        }
    }
}

@Composable
private fun ErrorBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(ErrorBadgeSize)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = ErrorContainerAlpha),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(ErrorIconSize),
        )
    }
}

@Preview("Failed Content")
@Preview("Failed Content (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FailedContentPreview() {
    ChessGymTheme {
        FailedContent()
    }
}

@Preview("Failed Content with retry")
@Preview("Failed Content with retry (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FailedContentWithRetryPreview() {
    ChessGymTheme {
        FailedContent(onRetry = {})
    }
}
