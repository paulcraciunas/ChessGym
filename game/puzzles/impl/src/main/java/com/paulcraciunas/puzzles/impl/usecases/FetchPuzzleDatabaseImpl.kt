package com.paulcraciunas.puzzles.impl.usecases

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.paulcraciunas.notifications.api.NotificationFactory
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FetchPuzzleDatabaseImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationFactory: NotificationFactory,
    private val dataAdapter: WorkInfoDataAdapter,
) : FetchPuzzleDatabase {

    override operator fun invoke(): Flow<FetchPuzzleDatabase.Progress> {
        notificationFactory.createChannel(context)

        val request = OneTimeWorkRequestBuilder<PuzzleSyncWorker>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(PuzzleSyncWorker.TAG)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(PuzzleSyncWorker.TAG, ExistingWorkPolicy.KEEP, request)

        return workManager.getWorkInfoByIdFlow(request.id).map { dataAdapter.adapt(it) }
    }
}
