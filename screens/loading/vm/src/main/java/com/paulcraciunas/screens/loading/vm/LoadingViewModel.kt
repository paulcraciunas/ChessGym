package com.paulcraciunas.screens.loading.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.global.device.api.usecases.GetFreeDiskSpace
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val getNetworkState: GetNetworkState,
    private val getFreeDiskSpace: GetFreeDiskSpace,
    private val fetchPuzzleDatabase: FetchPuzzleDatabase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoadingState>(LoadingState.ready())
    val uiState: StateFlow<LoadingState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Monitor network state for reactive error handling
            getNetworkState().collect { networkState ->
                val currentState = _uiState.value

                // Only handle network changes when in Ready state with network error
                if (currentState is LoadingState.Ready && currentState.error == LoadingState.Error.NoInternet) {
                    if (networkState == GetNetworkState.NetworkState.Connected) {
                        // Network recovered - clear the error and re-check conditions
                        _uiState.value = currentState.copy(error = LoadingState.Error.None)
                        checkDeviceConditions()
                    }
                }
            }
        }
    }

    fun onDownload() {
        _uiState.asState<LoadingState.Ready> { ready ->
            if (ready.requiresConfirmation) {
                _uiState.value = ready.copy(dialog = LoadingState.Dialog.Download)
            } else if (ready.requiresPermission) {
                _uiState.value = ready.copy(dialog = LoadingState.Dialog.Permission)
            } else {
                // Check device conditions before starting download
                checkDeviceConditions()
            }
        }
    }

    fun onDownloadConfirmation(accepted: Boolean) {
        if (accepted) {
            _uiState.value = LoadingState.downloadAccepted()
        } else {
            _uiState.value = LoadingState.ready()
        }
    }

    fun onPermissionReceived(isGranted: Boolean) {
        if (isGranted) {
            // Check device conditions before starting download
            checkDeviceConditions()
        } else {
            _uiState.value = LoadingState.permissionDenied()
        }
    }

    private fun checkDeviceConditions() {
        viewModelScope.launch {
            val currentNetworkState = getNetworkState().first()
            if (currentNetworkState == GetNetworkState.NetworkState.Disconnected) {
                _uiState.value = LoadingState.error(error = LoadingState.Error.NoInternet)
                return@launch
            }

            if (!getFreeDiskSpace().hasEnoughSpace(REQUIRED_DISK_SPACE_BYTES)) {
                _uiState.value = LoadingState.error(error = LoadingState.Error.NotEnoughDiskSpace)
                return@launch
            }

            // All checks passed, start download
            _uiState.value = LoadingState.Downloading()
            downloadPuzzles()
        }
    }

    private fun downloadPuzzles() {
        viewModelScope.launch {
            fetchPuzzleDatabase()
                .onCompletion {
                    if (it != null && it !is CancellationException) {
                        Log.e(TAG, "Unexpected error during puzzle database provisioning", it)
                        _uiState.value = LoadingState.runtimeError(LoadingState.Error.GenericRuntime)
                    }
                }
                .collect { progress ->
                    if (progress.hasError()) {
                        val error = when (progress.error) {
                            FetchPuzzleDatabase.Error.DownloadFailed -> LoadingState.Error.DownloadFailed
                            FetchPuzzleDatabase.Error.DecompressionFailed -> LoadingState.Error.DecompressionFailed
                            else -> LoadingState.Error.DatabaseWriteFailed
                        }
                        _uiState.value = LoadingState.runtimeError(error)
                        Log.w(TAG, "Download failed with error: $error")
                    } else if (progress.isComplete()) {
                        _uiState.value = LoadingState.Complete
                    } else {
                        _uiState.value = LoadingState.Downloading(
                            LoadingState.Downloading.Progress(
                                download = progress.download,
                                unpack = progress.unpack,
                                buildDb = progress.buildDb
                            )
                        )
                    }
                }
        }
    }

    companion object {
        // Required space for puzzle database: ~1.8GB for download, unpack, and final database
        private const val REQUIRED_DISK_SPACE_BYTES = 1_800_000_000L
        private const val TAG = "LoadingViewModel"
    }
}

private inline fun <reified T : LoadingState> MutableStateFlow<LoadingState>.asState(block: (T) -> Unit) {
    (value as? T)?.let { state ->
        block(state)
    } ?: Log.w(LoadingViewModel::class.java.canonicalName, "Wrong state found. Expected: ${T::class} found ${value::class}")
}
