package com.paulcraciunas.screens.about.vm

import com.paulcraciunas.domain.api.billing.BillingUseCase

interface AboutScreenInteractor {
    fun onDonateClicked()
    fun onDonateAmountSelected(product: BillingUseCase.DonationType)
    fun onDismissDonationDialog()
    fun onRateAppClicked()
}
