package com.paulcraciunas.chessgym.models

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
    val message: String,
    val code: ErrorCode,
)

@Serializable
data class SignInRequest(
    val deviceId: String,
    val displayName: String? = null,
)

@Serializable
data class SignInResponse(
    val userId: String,
    val user: UserDto,
    val isNewUser: Boolean,
)

@Serializable
data class AchievementStatistic(
    val achievementId: String,
    val tier: Int,
    val percentageOfUsers: Double,
)

@Serializable
data class AchievementStatisticsResponse(
    val totalUsers: Long,
    val achievements: List<AchievementStatistic>,
)
