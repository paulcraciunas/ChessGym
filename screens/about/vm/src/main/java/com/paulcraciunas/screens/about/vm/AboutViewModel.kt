package com.paulcraciunas.screens.about.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor() : ViewModel(), AboutScreenInteractor {

    private val _uiState: MutableStateFlow<AboutUiState> = MutableStateFlow(
        AboutUiState(libraries = provideLibraries())
    )
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    override fun onDonateClicked() {
        // TODO: Will be implemented with payment provider integration
    }

    override fun onRateAppClicked() {
        // TODO: Will be implemented with Play Store integration
    }

    private fun provideLibraries(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "Kotlin",
            url = "https://kotlinlang.org",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "Jetpack Compose",
            url = "https://developer.android.com/jetpack/compose",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "Material 3",
            url = "https://m3.material.io",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "Hilt",
            url = "https://dagger.dev/hilt",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "Kotlin Coroutines",
            url = "https://github.com/Kotlin/kotlinx.coroutines",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "AndroidX Navigation",
            url = "https://developer.android.com/jetpack/androidx/releases/navigation",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "AndroidX DataStore",
            url = "https://developer.android.com/jetpack/androidx/releases/datastore",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "AndroidX Room",
            url = "https://developer.android.com/jetpack/androidx/releases/room",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "AndroidX WorkManager",
            url = "https://developer.android.com/jetpack/androidx/releases/work",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "Kotlinx Serialization",
            url = "https://github.com/Kotlin/kotlinx.serialization",
            license = "Apache License 2.0",
        ),
    )
}
