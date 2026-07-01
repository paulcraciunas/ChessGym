package com.paulcraciunas.chessgym.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ErrorCode {
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    INTERNAL_ERROR,
}

@Serializable
data class ErrorResponse(
    @SerialName("message") val message: String,
    @SerialName("code") val code: ErrorCode,
)

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

@Serializable
data class AchievementStatistic(
    @SerialName("achievementId") val achievementId: String,
    @SerialName("tier") val tier: Int,
    @SerialName("percentageOfUsers") val percentageOfUsers: Double,
)

@Serializable
data class AchievementStatisticsResponse(
    @SerialName("totalUsers") val totalUsers: Long,
    @SerialName("achievements") val achievements: List<AchievementStatistic>,
)
