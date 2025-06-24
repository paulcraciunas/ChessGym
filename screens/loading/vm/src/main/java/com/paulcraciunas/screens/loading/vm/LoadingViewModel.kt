package com.paulcraciunas.screens.loading.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import com.paulcraciunas.settings.application.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    // TODO Paul: add NetworkConnection UseCase
    // TODO Paul: add EnoughDiskSpace UseCase
    private val fetchPuzzleDatabase: FetchPuzzleDatabase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoadingState>(LoadingState.Loading)
    val uiState: StateFlow<LoadingState> = _uiState.asStateFlow()

    init {
        // TODO Paul: lift this into the Loading UI so we can use the Splash Screen
        viewModelScope.launch {
            val appSettings = appSettingsRepository.appSettings.first()
            if (appSettings.puzzlesDownloaded) {
                _uiState.value = LoadingState.Complete
            } else {
                _uiState.value = LoadingState.ready()
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
                _uiState.value = LoadingState.Downloading()
                downloadPuzzles()
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
            _uiState.value = LoadingState.Downloading()
            downloadPuzzles()
        } else {
            _uiState.value = LoadingState.permissionDenied()
        }
    }

    private fun downloadPuzzles() {
        viewModelScope.launch {
            try {
                fetchPuzzleDatabase()
                    .collect { progress ->
                        if (progress.isComplete()) {
                            appSettingsRepository.updatePuzzlesDownloaded(true)
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
            } catch (e: Exception) {
                _uiState.value = LoadingState.downloadError()
            }
        }
    }
}

private inline fun <reified T : LoadingState> MutableStateFlow<LoadingState>.asState(block: (T) -> Unit) {
    (value as? T)?.let { state ->
        block(state)
    } ?: Log.w(LoadingViewModel::class.java.canonicalName, "Wrong state found. Expected: ${T::class} found ${value::class}")
}
