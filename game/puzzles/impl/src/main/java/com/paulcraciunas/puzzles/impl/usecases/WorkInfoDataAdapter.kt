package com.paulcraciunas.puzzles.impl.usecases

import androidx.work.WorkInfo
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import javax.inject.Inject

class WorkInfoDataAdapter @Inject constructor() {
    fun adapt(workInfo: WorkInfo?): FetchPuzzleDatabase.Progress {
        if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
            return FetchPuzzleDatabase.Progress(download = DONE, unpack = DONE, buildDb = DONE)
        } else if (workInfo?.state?.isFinished == true) {
            TODO("Paul: implement me. Propagate the error somehow")
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

    companion object {
        private const val NONE = 0
        private const val DONE = 100
    }
}
