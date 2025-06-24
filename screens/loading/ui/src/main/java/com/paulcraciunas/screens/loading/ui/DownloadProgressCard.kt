package com.paulcraciunas.screens.loading.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.loading.vm.LoadingState

@Composable
internal fun DownloadProgressCard(
    progress: LoadingState.Downloading.Progress
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.landing_download_content_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProgressItem(
                label = stringResource(R.string.landing_download_progress),
                progress = progress.download
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProgressItem(
                label = stringResource(R.string.landing_unpack_progress),
                progress = progress.unpack
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProgressItem(
                label = stringResource(R.string.landing_write_to_db_progress),
                progress = progress.buildDb
            )
        }
    }
}

@Composable
private fun ProgressItem(
    label: String,
    progress: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label: $progress%",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = if (progress == 100) Color.Green else MaterialTheme.colorScheme.primary
        )
    }
}
