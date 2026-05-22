package com.paulcraciunas.screens.about.vm

import com.paulcraciunas.domain.api.billing.BillingUseCase
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AboutViewModelTest {
    private val appSettingsRepository = FakeAppSettingsRepository()
    private val billingUseCase = FakeBillingUseCase()
    private val underTest = AboutViewModel(
        appSettingsRepository = appSettingsRepository,
        billingUseCase = billingUseCase,
    )

    @Nested
    internal inner class Initialization {
        @Test
        fun `GIVEN default state WHEN initialized THEN libraries list is not empty`() {
            val state = underTest.uiState.value

            assertTrue(state.libraries.isNotEmpty())
        }

        @Test
        fun `GIVEN default state WHEN initialized THEN each library has a name`() {
            val state = underTest.uiState.value

            state.libraries.forEach { library ->
                assertTrue(library.name.isNotBlank())
            }
        }

        @Test
        fun `GIVEN default state WHEN initialized THEN each library has a url`() {
            val state = underTest.uiState.value

            state.libraries.forEach { library ->
                assertTrue(library.url.isNotBlank())
            }
        }

        @Test
        fun `GIVEN default state WHEN initialized THEN each library has a license`() {
            val state = underTest.uiState.value

            state.libraries.forEach { library ->
                assertTrue(library.license.isNotBlank())
            }
        }

        @Test
        fun `GIVEN default state WHEN initialized THEN no duplicate libraries exist`() {
            val state = underTest.uiState.value

            val uniqueNames = state.libraries.map { it.name }.toSet()
            assertTrue(uniqueNames.size == state.libraries.size)
        }

        @Test
        fun `GIVEN default state WHEN initialized THEN Stockfish is listed with GPL 3`() {
            val state = underTest.uiState.value
            val stockfish = state.libraries.find { it.name == "Stockfish" }

            assertTrue(stockfish != null)
            assertEquals("GNU GPL 3.0", stockfish!!.license)
            assertTrue(stockfish.url.contains("github.com"))
        }
    }

    @Nested
    internal inner class Interactions {
        @Test
        fun `GIVEN initialized WHEN donate clicked THEN donation dialog is shown`() {
            underTest.onDonateClicked()

            val state = underTest.uiState.value
            assertTrue(state.showDonationDialog)
        }

        @Test
        fun `GIVEN donation dialog shown WHEN dismissed THEN donation dialog is hidden`() {
            underTest.onDonateClicked()
            underTest.onDismissDonationDialog()

            val state = underTest.uiState.value
            assertTrue(!state.showDonationDialog)
        }

        @Test
        fun `GIVEN donation dialog shown WHEN amount selected THEN donation dialog is hidden and event is sent`() = runTest {
            val product = BillingUseCase.DonationType.Small
            underTest.onDonateClicked()
            underTest.onDonateAmountSelected(product)

            val state = underTest.uiState.value
            assertTrue(!state.showDonationDialog)

            val event = underTest.uiEvents.first()
            assertEquals(AboutEvent.Donate(product), event)
        }

        @Test
        fun `GIVEN initialized WHEN rate app clicked THEN hasRatedApp is updated`() = runTest {
            underTest.onRateAppClicked()

            val hasRated = appSettingsRepository.appSettings.first().hasRatedApp
            assertTrue(hasRated)
        }

        @Test
        fun `GIVEN initialized WHEN rate app clicked THEN rate app event is sent`() = runTest {
            underTest.onRateAppClicked()

            val event = underTest.uiEvents.first()
            assertEquals(AboutEvent.RateTheApp, event)
        }
    }
}

class FakeBillingUseCase : BillingUseCase {
    private val _events = MutableSharedFlow<BillingUseCase.PurchaseEvent>()
    override val events: Flow<BillingUseCase.PurchaseEvent> = _events

    override fun donate(event: BillingUseCase.Donate) {
        // No-op for this VM test as the VM only sends a UI event
    }
}
