package com.paulcraciunas.domain.api.auth

import com.paulcraciunas.user.api.AuthService
import com.paulcraciunas.user.api.User

interface AuthenticateUseCase {
    suspend operator fun invoke(credentials: Credentials): User

    sealed interface Credentials {
        suspend fun authenticate(authService: AuthService): User.AuthenticationState

        class Google(private val tokenSource: TokenSource) : Credentials {
            override suspend fun authenticate(authService: AuthService): User.AuthenticationState =
                authService.signInWithGoogleToken(tokenSource.get())
        }

        data class Email(val email: String, val password: String) : Credentials {
            override suspend fun authenticate(authService: AuthService): User.AuthenticationState =
                authService.signInWithEmail(email, password)
        }

        data class NewAccount(val email: String, val password: String) : Credentials {
            override suspend fun authenticate(authService: AuthService): User.AuthenticationState =
                authService.signUpWithEmail(email, password)
        }
    }
}
