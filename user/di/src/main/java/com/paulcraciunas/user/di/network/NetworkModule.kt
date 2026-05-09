package com.paulcraciunas.user.di.network

import android.content.Context
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.paulcraciunas.global.qualifiers.BackendUrl
import com.paulcraciunas.global.qualifiers.IsDebug
import com.paulcraciunas.user.api.AuthService
import com.paulcraciunas.user.api.SyncScheduler
import com.paulcraciunas.user.api.SyncState
import com.paulcraciunas.user.api.TokenProvider
import com.paulcraciunas.user.di.auth.FirebaseAuthService
import com.paulcraciunas.user.di.auth.FirebaseTokenProvider
import com.paulcraciunas.user.impl.sync.SyncPreferences
import com.paulcraciunas.user.impl.sync.WorkManagerSyncScheduler
import com.paulcraciunas.user.remote.client.HttpClientFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logger
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("unused") // Used by Hilt
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: FirebaseTokenProvider): TokenProvider

    @Binds
    @Singleton
    abstract fun bindAuthService(impl: FirebaseAuthService): AuthService

    @Binds
    @Singleton
    abstract fun bindSyncState(impl: SyncPreferences): SyncState

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler

    @Binds
    @Singleton
    abstract fun bindHttpLogger(impl: TimberKtorLogger): Logger

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideHttpClient(
            @BackendUrl baseUrl: String,
            @IsDebug isDebug: Boolean,
            logger: Logger,
            tokenProvider: TokenProvider,
        ): HttpClient = HttpClientFactory.create(baseUrl, logger, tokenProvider, isDebug)

        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
    }
}

@Singleton
class TimberKtorLogger @Inject constructor(): Logger {
    override fun log(message: String) {
        Timber.tag("Ktor").d(message)
    }
}
