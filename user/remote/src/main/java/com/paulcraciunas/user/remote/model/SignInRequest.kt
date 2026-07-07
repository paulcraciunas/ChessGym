package com.paulcraciunas.user.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    @SerialName("deviceId") val deviceId: String,
    @SerialName("displayName") val displayName: String? = null,
)

@Serializable
data class SignInResponse(
    @SerialName("userId") val userId: String,
    @SerialName("user") val user: UserDto,
    @SerialName("isNewUser") val isNewUser: Boolean,
)
