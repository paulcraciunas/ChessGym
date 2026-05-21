package com.paulcraciunas.domain.impl.auth

import com.paulcraciunas.domain.api.auth.AuthenticateUseCase
import com.paulcraciunas.user.api.AuthService
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import javax.inject.Inject

class AuthenticateUseCaseImpl @Inject constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository,
) : AuthenticateUseCase {

    override suspend fun invoke(credentials: AuthenticateUseCase.Credentials): User {
        val authResult = credentials.authenticate(authService)
        return userRepository.signIn(authResult)
    }
}
