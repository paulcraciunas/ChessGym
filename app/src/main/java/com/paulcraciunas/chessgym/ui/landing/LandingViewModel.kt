package com.paulcraciunas.chessgym.ui.landing

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import com.paulcraciunas.notifications.api.NotificationFactory
import com.paulcraciunas.settings.user.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

//TODO Paul: This is only temporary. Delete this and reimplement it properly
@HiltViewModel
class LandingViewModel @Inject constructor(
    private val userSettings: UserSettings,
    private val notificationFactory: NotificationFactory,
) : ViewModel() {
    private val _progress = MutableStateFlow(0)
    private val _progressStep = MutableStateFlow(PuzzleSyncWorker.Step.Download)
    private val _workInfoLiveData = MutableLiveData<WorkInfo?>()

    val progress: StateFlow<Int> = _progress.asStateFlow()
    val step: StateFlow<PuzzleSyncWorker.Step> = _progressStep.asStateFlow()
    val workInfoLiveData: LiveData<WorkInfo?> = _workInfoLiveData

    private var currentWorkId: UUID? = null

    fun startPuzzleDownload(context: Context) {
        notificationFactory.createChannel(context)

        val request = OneTimeWorkRequestBuilder<PuzzleSyncWorker>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .addTag(PuzzleSyncWorker.TAG)
            .build()

        currentWorkId = request.id
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(PuzzleSyncWorker.TAG, ExistingWorkPolicy.KEEP, request)

        observeWork(workManager, request.id)
    }

    private fun observeWork(workManager: WorkManager, workId: UUID) {
        workManager.getWorkInfoByIdLiveData(workId).observeForever { workInfo ->
            _workInfoLiveData.postValue(workInfo)

            val percent = workInfo?.progress?.getInt(PuzzleSyncWorker.PROGRESS_NAME, 0) ?: 0
            _progress.value = percent
            _progressStep.value =
                PuzzleSyncWorker.Step.valueOf(
                    workInfo?.progress?.getString(PuzzleSyncWorker.STEP) ?: PuzzleSyncWorker.Step.Download.toString()
                )

            if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                userSettings.puzzlesDownloaded = true
            }
        }
    }
}
