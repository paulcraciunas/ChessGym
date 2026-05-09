package com.paulcraciunas.domain.api.auth

fun interface TokenSource {
    suspend fun get(): String
}
