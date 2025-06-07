package com.paulcraciunas.chessgym.ui.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import com.paulcraciunas.chessgym.R
import com.paulcraciunas.data.network.PuzzleSyncWorker
import com.paulcraciunas.settings.user.UserSettings

//TODO Paul: This is only temporary. Delete this and reimplement it properly
@Composable
fun LandingScreen(
    settings: UserSettings,
    viewModel: LandingViewModel,
    modifier: Modifier = Modifier
) {
    val workInfo = viewModel.workInfoLiveData.observeAsState().value
    val isDownloading = workInfo?.state == WorkInfo.State.RUNNING
    val isDownloaded = settings.puzzlesDownloaded || workInfo?.state == WorkInfo.State.SUCCEEDED
    val progress by viewModel.progress.collectAsState()
    val step by viewModel.step.collectAsState()
    val context = LocalContext.current

    if (!isDownloaded) {
        DownloadPromptScreen(
            isDownloading = isDownloading,
            onStartDownload = { viewModel.startPuzzleDownload(context) },
            progress,
            step,
            modifier
        )
    } else {
        UserStatsScreen(settings, modifier)
    }
}

@Composable
fun DownloadPromptScreen(
    isDownloading: Boolean,
    onStartDownload: () -> Unit,
    progress: Int,
    step: PuzzleSyncWorker.Step, // TODO Paul: this should be abstracted away
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isDownloading) {
            CircularProgressIndicator(
                progress = { progress / 100f },
                trackColor = ProgressIndicatorDefaults.circularDeterminateTrackColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    when (step) {
                        PuzzleSyncWorker.Step.Download -> R.string.download_progress
                        PuzzleSyncWorker.Step.Unpack -> R.string.unpack_progress
                        PuzzleSyncWorker.Step.BuildDb -> R.string.write_to_db_progress
                    },
                    progress
                )
            )
        } else {
            Text(text = stringResource(id = R.string.download_prompt))
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onStartDownload) {
                Text(text = stringResource(id = R.string.download_button))
            }
        }
    }
}

@Composable
fun UserStatsScreen(
    settings: UserSettings,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Puzzles Played This Week: ${settings.playedWeek}")
        Text("This Month: ${settings.playedMonth}")
        Text("Success Rate: ${settings.successRate}%")
        Text("Current Rating: ${settings.rating}")
        Text("High Score: ${settings.highScore}")
        Spacer(modifier = Modifier.height(24.dp))

        // Toolbar with action
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { /* Start new challenge */ }) {
                Text("New Challenge")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadPromptScreenPreview_Idle() {
    DownloadPromptScreen(
        isDownloading = false,
        onStartDownload = {},
        progress = 0,
        step = PuzzleSyncWorker.Step.Download
    )
}

@Preview(showBackground = true)
@Composable
fun DownloadPromptScreenPreview_Downloading() {
    DownloadPromptScreen(
        isDownloading = true,
        onStartDownload = {},
        progress = 45,
        step = PuzzleSyncWorker.Step.BuildDb
    )
}
