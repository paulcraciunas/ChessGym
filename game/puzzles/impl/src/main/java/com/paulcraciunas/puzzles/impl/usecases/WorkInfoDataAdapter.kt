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
            return FetchPuzzleDatabase.Progress(download = DONE, unpack = DONE, buildDb = DONE)
        } else if (workInfo?.state?.isFinished == true) {
            // Handle failed states
            return when (workInfo.state) {
                WorkInfo.State.FAILED -> {
                    val errorType = workInfo.outputData.getString(PuzzleSyncWorker.ERROR_TYPE)
                    val failedStep = workInfo.outputData.getString(PuzzleSyncWorker.FAILED_STEP)
                    Timber.w("Work failed at step: $failedStep, error: $errorType")
                    
                    val error = mapErrorType(errorType)
                    val (download, unpack, buildDb) = calculateProgressFromFailedStep(failedStep)
                    
                    FetchPuzzleDatabase.Progress(
                        download = download, 
                        unpack = unpack, 
                        buildDb = buildDb, 
                        error = error
                    )
                }
                WorkInfo.State.CANCELLED -> {
                    Timber.w("Work was cancelled")
                    FetchPuzzleDatabase.Progress(
                        download = NONE, 
                        unpack = NONE, 
                        buildDb = NONE, 
                        error = FetchPuzzleDatabase.Error.None
                    )
                }
                else -> {
                    Timber.w("Work finished with unexpected state: ${workInfo.state}")
                    FetchPuzzleDatabase.Progress(
                        download = NONE, 
                        unpack = NONE, 
                        buildDb = NONE, 
                        error = FetchPuzzleDatabase.Error.None
                    )
                }
            }
        }
        
        val data = workInfo?.progress ?: return FetchPuzzleDatabase.Progress(download = NONE, unpack = NONE, buildDb = NONE)

        var percent = data.getInt(PuzzleSyncWorker.PROGRESS_NAME, NONE)
        if (percent < NONE) percent = NONE
        if (percent > DONE) percent = DONE
        val step = PuzzleSyncWorker.Step.fromString(data.getString(PuzzleSyncWorker.STEP)) ?: PuzzleSyncWorker.Step.Download
        return when (step) {
            PuzzleSyncWorker.Step.Download -> FetchPuzzleDatabase.Progress(download = percent, unpack = NONE, buildDb = NONE)
            PuzzleSyncWorker.Step.Unpack -> FetchPuzzleDatabase.Progress(download = DONE, unpack = percent, buildDb = NONE)
            PuzzleSyncWorker.Step.BuildDb -> FetchPuzzleDatabase.Progress(download = DONE, unpack = DONE, buildDb = percent)
        }
    }
    
    private fun mapErrorType(errorType: String?): FetchPuzzleDatabase.Error {
        return when (errorType) {
            PuzzleSyncWorker.ERROR_DOWNLOAD_FAILED -> FetchPuzzleDatabase.Error.DownloadFailed
            PuzzleSyncWorker.ERROR_DECOMPRESSION_FAILED -> FetchPuzzleDatabase.Error.DecompressionFailed
            PuzzleSyncWorker.ERROR_DATABASE_WRITE_FAILED -> FetchPuzzleDatabase.Error.DatabaseWriteFailed
            else -> FetchPuzzleDatabase.Error.None
        }
    }
    
    private fun calculateProgressFromFailedStep(failedStep: String?): Triple<Int, Int, Int> {
        return when (PuzzleSyncWorker.Step.fromString(failedStep)) {
            PuzzleSyncWorker.Step.Download -> Triple(NONE, NONE, NONE) // Failed during download
            PuzzleSyncWorker.Step.Unpack -> Triple(DONE, NONE, NONE) // Download completed, unpack failed
            PuzzleSyncWorker.Step.BuildDb -> Triple(DONE, DONE, NONE) // Download and unpack completed, build failed
            else -> Triple(NONE, NONE, NONE) // Unknown failure
        }
    }

    companion object {
        private const val NONE = 0
        private const val DONE = 100
    }
}
