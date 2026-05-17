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
import kotlinx.coroutines.delay
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

    private var provisioningJob: Job? = null
    private var factRotationJob: Job? = null

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

    fun onTierSelected(tier: DatabaseTier) {
        _uiState.update { state ->
            val ready = state as? LoadingState.Ready ?: return@update state
            ready.copy(selectedTier = tier)
        }
    }

    fun onDownload() {
        val newState = _uiState.updateAndGet { state ->
            val ready = state as? LoadingState.Ready ?: return@updateAndGet state
            when {
                !crashReportingConsent.value -> ready.copy(dialog = LoadingState.Dialog.CrashConsent)
                ready.selectedTier.isBundled -> ready.copy(
                    requiresConfirmation = false,
                    requiresPermission = false,
                    dialog = LoadingState.Dialog.None,
                    error = LoadingState.Error.None,
                )
                ready.requiresConfirmation -> ready.copy(dialog = LoadingState.Dialog.Download)
                ready.requiresPermission -> ready.copy(dialog = LoadingState.Dialog.Permission)
                else -> ready.copy(error = LoadingState.Error.None)
            }
        }

        if (newState is LoadingState.Ready && newState.dialog == LoadingState.Dialog.None) {
            if (newState.selectedTier.isBundled) {
                provisionBundledTier()
            } else {
                checkDeviceConditions()
            }
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

    private fun provisionBundledTier() {
        provisioningJob = viewModelScope.launch {
            _uiState.update { LoadingState.Complete }
            delay(COMPLETION_HOLD_MS)
            appSettingsRepository.updatePuzzlesDownloaded(true)
        }
    }

    private fun checkDeviceConditions() {
        if (provisioningJob?.isActive == true
            || _uiState.value is LoadingState.Complete) return

        val selectedTier = (_uiState.value as? LoadingState.Ready)?.selectedTier ?: DatabaseTier.DEFAULT

        provisioningJob = viewModelScope.launch {
            val currentNetworkState = getNetworkState().first()
            if (currentNetworkState == GetNetworkState.NetworkState.Disconnected) {
                _uiState.update { LoadingState.error(error = LoadingState.Error.NoInternet) }
                return@launch
            }
            if (!getFreeDiskSpace().hasEnoughSpace(selectedTier.requiredDiskSpaceBytes())) {
                _uiState.update { LoadingState.error(error = LoadingState.Error.NotEnoughDiskSpace) }
                return@launch
            }
            downloadPuzzles(selectedTier)
        }
    }

    private suspend fun downloadPuzzles(tier: DatabaseTier) {
        _uiState.update { LoadingState.Downloading() }
        startFactRotation()

        fetchPuzzleDatabase(tier.tierSegment)
            .onCompletion {
                stopFactRotation()
                if (it != null && it !is CancellationException) {
                    Timber.e(it, "Unexpected error during puzzle database provisioning")
                    _uiState.update { LoadingState.runtimeError(LoadingState.Error.GenericRuntime) }
                }
            }
            .collect(::updateDownloadProgress)
    }

    private fun startFactRotation() {
        factRotationJob?.cancel()
        factRotationJob = viewModelScope.launch {
            while (true) {
                delay(FACT_ROTATION_INTERVAL_MS)
                _uiState.update { state ->
                    val downloading = state as? LoadingState.Downloading ?: return@update state
                    downloading.copy(factIndex = downloading.factIndex + 1)
                }
            }
        }
    }

    private fun stopFactRotation() {
        factRotationJob?.cancel()
        factRotationJob = null
    }

    private fun updateDownloadProgress(progress: FetchPuzzleDatabase.Progress) {
        _uiState.update { currentState ->
            when {
                progress.hasError() -> {
                    Timber.w("Download failed with error: ${progress.error}")
                    LoadingState.runtimeError(progress.error.toLoadingState())
                }
                progress.isComplete() -> {
                    viewModelScope.launch {
                        delay(COMPLETION_HOLD_MS)
                        appSettingsRepository.updatePuzzlesDownloaded(true)
                    }
                    LoadingState.Complete
                }
                else -> {
                    val currentFactIndex = (currentState as? LoadingState.Downloading)?.factIndex ?: 0
                    LoadingState.Downloading(
                        progress = LoadingState.Downloading.Progress(
                            download = progress.download,
                            unpack = progress.unpack,
                        ),
                        factIndex = currentFactIndex,
                    )
                }
            }
        }
    }

    companion object {
        private const val COMPLETION_HOLD_MS = 1_000L
        private const val FACT_ROTATION_INTERVAL_MS = 10_000L

        private fun DatabaseTier.requiredDiskSpaceBytes(): Long = when (this) {
            DatabaseTier.Full -> 525_000_000L
            DatabaseTier.Compact -> 200_000_000L
            DatabaseTier.Lite -> 50_000_000L
        }
    }
}

private fun FetchPuzzleDatabase.Error.toLoadingState(): LoadingState.Error = when (this) {
    FetchPuzzleDatabase.Error.DownloadFailed -> LoadingState.Error.DownloadFailed
    FetchPuzzleDatabase.Error.DecompressionFailed -> LoadingState.Error.DecompressionFailed
    else -> LoadingState.Error.DatabaseWriteFailed
}
