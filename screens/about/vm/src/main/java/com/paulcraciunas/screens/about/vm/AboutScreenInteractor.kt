package com.paulcraciunas.screens.about.vm

import com.paulcraciunas.global.billing.BillingManager

interface AboutScreenInteractor {
    fun onDonateClicked()
    fun onDonateAmountSelected(product: BillingManager.DonationType)
    fun onDismissDonationDialog()
    fun onRateAppClicked()
}
