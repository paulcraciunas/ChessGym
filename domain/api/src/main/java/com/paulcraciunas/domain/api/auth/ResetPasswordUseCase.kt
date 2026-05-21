package com.paulcraciunas.domain.api.auth

interface ResetPasswordUseCase {
    suspend operator fun invoke(email: String)
}
