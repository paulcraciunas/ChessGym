package com.paulcraciunas.global.billing

import android.app.Activity
import com.paulcraciunas.domain.api.billing.BillingUseCase
import com.paulcraciunas.domain.api.billing.BillingUseCase.DonationType

class PlayStoreDonate(val activity: Activity, type: DonationType) : BillingUseCase.Donate(type)
