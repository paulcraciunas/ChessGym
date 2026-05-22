package com.paulcraciunas.screens.about.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.global.billing.BillingManager

@Immutable
data class AboutUiState(
    val libraries: List<LibraryInfo> = emptyList(),
    val showDonationDialog: Boolean = false,
)

@Immutable
data class LibraryInfo(
    val name: String,
    val url: String,
    val license: String,
)

@Immutable
sealed interface AboutEvent {
    data object RateTheApp : AboutEvent
    data class Donate(val product: BillingManager.DonationType) : AboutEvent
}
