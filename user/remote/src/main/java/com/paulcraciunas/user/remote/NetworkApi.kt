package com.paulcraciunas.user.remote

import com.paulcraciunas.global.qualifiers.BackendUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkApi @Inject constructor(
    @param:BackendUrl private val baseUrl: String,
) {
    val v1 = V1(root = baseUrl)

    interface Endpoint {
        val signIn: String
        fun user(id: String): String
    }

    class V1(root: String) : Endpoint {
        private val apiBase = "$root/api/v1"

        override val signIn: String
            get() = "$apiBase/auth/signin"

        override fun user(id: String): String = "$apiBase/users/$id"
    }
}
