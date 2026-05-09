package com.paulcraciunas.domain.impl.auth

import com.paulcraciunas.domain.api.auth.SignOutUseCase
import com.paulcraciunas.user.api.SyncScheduler
import com.paulcraciunas.user.api.SyncState
import com.paulcraciunas.user.api.TokenProvider
import com.paulcraciunas.user.api.UserRepository
import javax.inject.Inject

class SignOutUseCaseImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
    private val syncState: SyncState,
    private val syncScheduler: SyncScheduler,
) : SignOutUseCase {

    override suspend fun invoke() {
        syncScheduler.cancel()
        syncState.clear()
        tokenProvider.signOut()
        userRepository.signOut()
    }
}
