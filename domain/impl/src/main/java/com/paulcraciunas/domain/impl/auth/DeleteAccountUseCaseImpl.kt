package com.paulcraciunas.domain.impl.auth

import com.paulcraciunas.domain.api.auth.DeleteAccountResult
import com.paulcraciunas.domain.api.auth.DeleteAccountUseCase
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import com.paulcraciunas.global.device.api.usecases.GetNetworkState.NetworkState
import com.paulcraciunas.user.api.AuthService
import com.paulcraciunas.user.api.SyncScheduler
import com.paulcraciunas.user.api.SyncState
import com.paulcraciunas.user.api.TokenProvider
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteAccountUseCaseImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val tokenProvider: TokenProvider,
    private val syncState: SyncState,
    private val syncScheduler: SyncScheduler,
    private val getNetworkState: GetNetworkState,
) : DeleteAccountUseCase {

    override suspend fun invoke(): DeleteAccountResult {
        val networkState = getNetworkState().first()
        if (networkState == NetworkState.Disconnected) {
            return DeleteAccountResult.NoNetwork
        }

        return try {
            syncScheduler.cancel()
            userRepository.clear()
            authService.deleteAccount()
            syncState.clear()
            tokenProvider.signOut()
            DeleteAccountResult.Success
        } catch (e: Exception) {
            DeleteAccountResult.Failure(e)
        }
    }
}
