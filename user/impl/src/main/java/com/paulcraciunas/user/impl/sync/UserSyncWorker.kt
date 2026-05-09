package com.paulcraciunas.user.impl.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.paulcraciunas.user.api.SyncState
import com.paulcraciunas.user.api.UserLocalDataSource
import com.paulcraciunas.user.api.UserRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class UserSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataSource: UserLocalDataSource,
    private val remoteDataSource: UserRemoteDataSource,
    private val syncPreferences: SyncState,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val user = localDataSource.getUser()
        if (!user.isSignedIn()) {
            return Result.success()
        }

        if (!syncPreferences.isDirty()) {
            return Result.success()
        }

        return try {
            remoteDataSource.updateUser(user)
            syncPreferences.markClean()
            Timber.d("User data synced successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.w(e, "User sync failed, will retry")
            Result.retry()
        }
    }

    companion object {
        const val TAG = "UserSync"
    }
}
