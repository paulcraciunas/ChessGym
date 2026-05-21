package com.paulcraciunas.domain.impl.auth

import com.paulcraciunas.domain.api.auth.ResetPasswordUseCase
import com.paulcraciunas.user.api.AuthService
import javax.inject.Inject

class ResetPasswordUseCaseImpl @Inject constructor(
    private val authService: AuthService,
) : ResetPasswordUseCase {

    override suspend fun invoke(email: String) {
        authService.sendPasswordResetEmail(email)
    }
}
