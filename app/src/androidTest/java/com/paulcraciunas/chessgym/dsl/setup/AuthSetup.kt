package com.paulcraciunas.chessgym.dsl.setup

import com.paulcraciunas.user.api.FakeAuthService

class AuthSetup(private val authService: FakeAuthService) {

    fun withExistingUser(email: String, password: String, id: String = DEFAULT_USER_ID): AuthSetup = apply {
        authService.withExistingUser(email, password, id, USER_NAME)
    }

    private companion object {
        const val DEFAULT_USER_ID: String = "test-auth-user-id"
        const val USER_NAME: String = "DarthVader"
    }
}
