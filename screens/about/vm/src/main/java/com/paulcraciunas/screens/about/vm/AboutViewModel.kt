package com.paulcraciunas.screens.about.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.billing.BillingUseCase
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    val billingUseCase: BillingUseCase,
) : ViewModel(), AboutScreenInteractor {
    private val _aboutEvents = Channel<AboutEvent>(Channel.BUFFERED)
    private val _uiState = MutableStateFlow(AboutUiState(libraries = provideLibraries()))
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()
    val uiEvents = _aboutEvents.receiveAsFlow()

    override fun onDonateClicked() {
        _uiState.value = _uiState.value.copy(showDonationDialog = true)
    }

    override fun onDonateAmountSelected(product: BillingUseCase.DonationType) {
        _uiState.value = _uiState.value.copy(showDonationDialog = false)
        viewModelScope.launch {
            _aboutEvents.send(AboutEvent.Donate(product))
        }
    }

    override fun onDismissDonationDialog() {
        _uiState.value = _uiState.value.copy(showDonationDialog = false)
    }

    override fun onRateAppClicked() {
        viewModelScope.launch {
            appSettingsRepository.updateHasRatedApp(true)
            _aboutEvents.send(AboutEvent.RateTheApp)
        }
    }

    private fun provideLibraries(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "Stockfish",
            url = "https://github.com/official-stockfish/Stockfish",
            license = "GNU GPL 3.0",
        ),
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
        LibraryInfo(
            name = "Ktor",
            url = "https://ktor.io",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "Firebase",
            url = "https://firebase.google.com",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "Timber",
            url = "https://github.com/JakeWharton/timber",
            license = "Apache License 2.0",
        ),
        LibraryInfo(
            name = "Zstandard (zstd-jni)",
            url = "https://github.com/luben/zstd-jni",
            license = "BSD 2-Clause",
        ),
        LibraryInfo(
            name = "Google Play Billing",
            url = "https://developer.android.com/google/play/billing",
            license = "Apache License 2.0",
        ),
    )
}
