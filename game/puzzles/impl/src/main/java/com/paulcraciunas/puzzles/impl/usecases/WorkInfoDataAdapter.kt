package com.paulcraciunas.puzzles.impl.usecases

//noinspection PureDomain
import androidx.work.WorkInfo
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import timber.log.Timber
import javax.inject.Inject

class WorkInfoDataAdapter @Inject constructor() {
    fun adapt(workInfo: WorkInfo?): FetchPuzzleDatabase.Progress {
        if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
            return FetchPuzzleDatabase.Progress(download = DONE, unpack = DONE)
        } else if (workInfo?.state?.isFinished == true) {
            return adaptFinishedState(workInfo)
        }
        val data = workInfo?.progress ?: return FetchPuzzleDatabase.Progress(download = NONE, unpack = NONE)
        val percent = data.getInt(PuzzleSyncWorker.PROGRESS_NAME, NONE).coerceIn(NONE, DONE)
        val step = PuzzleSyncWorker.Step.fromString(data.getString(PuzzleSyncWorker.STEP)) ?: PuzzleSyncWorker.Step.Download
        return when (step) {
            PuzzleSyncWorker.Step.Download -> FetchPuzzleDatabase.Progress(download = percent, unpack = NONE)
            PuzzleSyncWorker.Step.Unpack -> FetchPuzzleDatabase.Progress(download = DONE, unpack = percent)
        }
    }

    private fun adaptFinishedState(workInfo: WorkInfo): FetchPuzzleDatabase.Progress =
        when (workInfo.state) {
            WorkInfo.State.FAILED -> {
                val errorType = workInfo.outputData.getString(PuzzleSyncWorker.ERROR_TYPE)
                val failedStep = workInfo.outputData.getString(PuzzleSyncWorker.FAILED_STEP)
                Timber.w("Work failed at step: $failedStep, error: $errorType")

                val error = mapErrorType(errorType)
                val (download, unpack) = calculateProgressFromFailedStep(failedStep)
                FetchPuzzleDatabase.Progress(download = download, unpack = unpack, error = error)
            }
            WorkInfo.State.CANCELLED -> {
                Timber.w("Work was cancelled")
                FetchPuzzleDatabase.Progress(download = NONE, unpack = NONE)
            }
            else -> {
                Timber.w("Work finished with unexpected state: ${workInfo.state}")
                FetchPuzzleDatabase.Progress(download = NONE, unpack = NONE)
            }
        }

    private fun mapErrorType(errorType: String?): FetchPuzzleDatabase.Error = when (errorType) {
        PuzzleSyncWorker.ERROR_DOWNLOAD_FAILED -> FetchPuzzleDatabase.Error.DownloadFailed
        PuzzleSyncWorker.ERROR_DECOMPRESSION_FAILED -> FetchPuzzleDatabase.Error.DecompressionFailed
        PuzzleSyncWorker.ERROR_DATABASE_WRITE_FAILED -> FetchPuzzleDatabase.Error.DatabaseWriteFailed
        else -> FetchPuzzleDatabase.Error.None
    }

    private fun calculateProgressFromFailedStep(failedStep: String?): Pair<Int, Int> =
        when (PuzzleSyncWorker.Step.fromString(failedStep)) {
            PuzzleSyncWorker.Step.Download -> Pair(NONE, NONE)
            PuzzleSyncWorker.Step.Unpack -> Pair(DONE, NONE)
            else -> Pair(NONE, NONE)
        }

    companion object {
        private const val NONE = 0
        private const val DONE = 100
    }
}
