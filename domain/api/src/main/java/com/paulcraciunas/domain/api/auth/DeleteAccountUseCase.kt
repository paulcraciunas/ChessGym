package com.paulcraciunas.domain.api.auth

sealed class DeleteAccountResult {
    data object Success : DeleteAccountResult()
    data object NoNetwork : DeleteAccountResult()
    data class Failure(val cause: Throwable) : DeleteAccountResult()
}

interface DeleteAccountUseCase {
    suspend operator fun invoke(): DeleteAccountResult
}
