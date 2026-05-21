package com.paulcraciunas.user.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    val deviceId: String,
    val displayName: String? = null,
)
