package com.paulcraciunas.global.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.consumePurchase
import com.paulcraciunas.domain.api.billing.BillingUseCase
import com.paulcraciunas.global.qualifiers.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @IoDispatcher dispatcher: CoroutineDispatcher,
) : PurchasesUpdatedListener, BillingUseCase.BillingEngine {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    override val events = Channel<BillingUseCase.PurchaseEvent>()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    override fun start() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing setup finished successfully")
                } else {
                    Timber.e("Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing service disconnected")
                // Try to restart the connection on the next request to Google Play
            }
        })
    }

    override fun accept(event: BillingUseCase.Donate) {
        if (event !is PlayStoreDonate) {
            Timber.e("Wrong event type sent to PlayStore billing manager: ${event.javaClass}")
            return
        }
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(event.type.id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsResult.productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsResult.productDetailsList[0]
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    )
                    .build()

                billingClient.launchBillingFlow(event.activity, billingFlowParams)
            } else {
                Timber.e("Failed to query product details: ${billingResult.debugMessage}")
                scope.launch {
                    events.send(element = BillingUseCase.PurchaseEvent.Error("Failed to load donation details"))
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK if purchases != null -> {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.i("User canceled the purchase")
            }
            else -> {
                Timber.e("Purchase updated failed: ${billingResult.debugMessage}")
                scope.launch {
                    events.send(element = BillingUseCase.PurchaseEvent.Error("Purchase failed: ${billingResult.debugMessage}"))
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Donations are consumables so they can be bought again
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            scope.launch {
                val result = billingClient.consumePurchase(consumeParams)
                if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Purchase consumed successfully")
                    events.send(element = BillingUseCase.PurchaseEvent.Success(BillingUseCase.DonationType.fromId(purchase.products.firstOrNull())))
                } else {
                    Timber.e("Failed to consume purchase: ${result.billingResult.debugMessage}")
                    events.send(element = BillingUseCase.PurchaseEvent.Error("Failed to complete donation process"))
                }
            }
        }
    }
}
