package com.paulcraciunas.domain.api.billing

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

interface BillingUseCase {
    val events: Flow<PurchaseEvent>

    fun donate(event: Donate)

    abstract class Donate(val type: DonationType)

    enum class DonationType(val id: String) {
        Small(id = "donation_small"),
        Medium(id = "donation_medium"),
        Large(id = "donation_large");

        companion object {
            fun fromId(id: String?): DonationType = when (id) {
                "donation_small" -> Small
                "donation_medium" -> Medium
                "donation_large" -> Large
                else -> Small
            }
        }
    }

    sealed interface PurchaseEvent {
        data class Success(val donation: DonationType) : PurchaseEvent
        data class Error(val message: String) : PurchaseEvent
    }

    interface BillingEngine {
        val events: Channel<PurchaseEvent>

        fun start()
        fun accept(event: Donate)
    }
}
