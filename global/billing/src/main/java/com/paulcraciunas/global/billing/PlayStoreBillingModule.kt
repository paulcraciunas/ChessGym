package com.paulcraciunas.global.billing

import com.paulcraciunas.domain.api.billing.BillingUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PlayStoreBillingModule {
    @Binds
    @Singleton
    abstract fun bindBillingEngine(impl: BillingManager): BillingUseCase.BillingEngine
}
