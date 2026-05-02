package com.paulcraciunas.screens.loading.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.global.device.api.usecases.GetFreeDiskSpace
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val getNetworkState: GetNetworkState,
    private val getFreeDiskSpace: GetFreeDiskSpace,
    private val fetchPuzzleDatabase: FetchPuzzleDatabase,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val crashReportingConsent = appSettingsRepository.appSettings
        .map { it.crashReportingConsent }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _uiState = MutableStateFlow<LoadingState>(LoadingState.ready())
    val uiState: StateFlow<LoadingState> = _uiState.asStateFlow()

    // track the provisioning process (checks + download) to avoid overlaps
    private var provisioningJob: Job? = null

    init {
        observeNetworkRecovery()
    }

    private fun observeNetworkRecovery() {
        getNetworkState()
            .onEach { networkState ->
                val state = _uiState.value
                if (state is LoadingState.Ready &&
                    state.error == LoadingState.Error.NoInternet &&
                    networkState == GetNetworkState.NetworkState.Connected
                ) {
                    _uiState.update { (it as LoadingState.Ready).copy(error = LoadingState.Error.None) }
                    checkDeviceConditions()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onDownload() {
        // Use updateAndGet to atomically update and get the RESULTING state
        val newState = _uiState.updateAndGet { state ->
            val ready = state as? LoadingState.Ready ?: return@updateAndGet state
            when {
                !crashReportingConsent.value -> ready.copy(dialog = LoadingState.Dialog.CrashConsent)
                ready.requiresConfirmation -> ready.copy(dialog = LoadingState.Dialog.Download)
                ready.requiresPermission -> ready.copy(dialog = LoadingState.Dialog.Permission)
                else -> ready.copy(error = LoadingState.Error.None)
            }
        }

        // Trigger side effects only if the NEW state is "Ready" with no dialogs
        if (newState is LoadingState.Ready && newState.dialog == LoadingState.Dialog.None) {
            checkDeviceConditions()
        }
    }

    fun onCrashConsentResponse(accepted: Boolean) {
        viewModelScope.launch {
            if (accepted) {
                appSettingsRepository.updateCrashReportingConsent(true)
                _uiState.update { LoadingState.consentAccepted() }
            } else {
                _uiState.update { LoadingState.consentDeclined() }
            }
        }
    }

    fun onDownloadConfirmation(accepted: Boolean) {
        _uiState.update { if (accepted) LoadingState.downloadAccepted() else LoadingState.ready() }
    }

    fun onPermissionReceived(isGranted: Boolean) {
        if (isGranted) {
            checkDeviceConditions()
        } else {
            _uiState.update { LoadingState.permissionDenied() }
        }
    }

    private fun checkDeviceConditions() {
        if (provisioningJob?.isActive == true
            || _uiState.value is LoadingState.Complete) return

        provisioningJob = viewModelScope.launch {
            val currentNetworkState = getNetworkState().first()
            if (currentNetworkState == GetNetworkState.NetworkState.Disconnected) {
                _uiState.update { LoadingState.error(error = LoadingState.Error.NoInternet) }
                return@launch
            }
            if (!getFreeDiskSpace().hasEnoughSpace(REQUIRED_DISK_SPACE_BYTES)) {
                _uiState.update { LoadingState.error(error = LoadingState.Error.NotEnoughDiskSpace) }
                return@launch
            }
            // All checks passed, start download
            downloadPuzzles()
        }
    }

    private suspend fun downloadPuzzles() {
        _uiState.update { LoadingState.Downloading() }

        fetchPuzzleDatabase()
            .onCompletion {
                if (it != null && it !is CancellationException) {
                    Timber.e(it, "Unexpected error during puzzle database provisioning")
                    _uiState.update { LoadingState.runtimeError(LoadingState.Error.GenericRuntime) }
                }
            }
            .collect(::updateDownloadProgress)
    }

    private fun updateDownloadProgress(progress: FetchPuzzleDatabase.Progress) {
        _uiState.update {
            when {
                progress.hasError() -> {
                    Timber.w("Download failed with error: ${progress.error}")
                    LoadingState.runtimeError(progress.error.toLoadingState())
                }
                progress.isComplete() -> LoadingState.Complete
                else -> LoadingState.Downloading(
                    LoadingState.Downloading.Progress(
                        download = progress.download,
                        unpack = progress.unpack,
                        buildDb = progress.buildDb
                    )
                )
            }
        }
    }

    companion object {
        // Required space for puzzle database: ~1.8GB for download, unpack, and final database
        private const val REQUIRED_DISK_SPACE_BYTES = 1_800_000_000L
    }
}

private fun FetchPuzzleDatabase.Error.toLoadingState(): LoadingState.Error = when (this) {
    FetchPuzzleDatabase.Error.DownloadFailed -> LoadingState.Error.DownloadFailed
    FetchPuzzleDatabase.Error.DecompressionFailed -> LoadingState.Error.DecompressionFailed
    else -> LoadingState.Error.DatabaseWriteFailed
}
