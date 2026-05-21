package com.paulcraciunas.chessgym.services

import com.paulcraciunas.chessgym.models.ProfileDto
import com.paulcraciunas.chessgym.models.UserDto
import com.paulcraciunas.chessgym.repositories.UserRepository

class DefaultUserService(
    private val userRepository: UserRepository,
    private val mergeStrategy: UserMergeStrategy = UserMergeStrategy(),
) : UserService {

    override suspend fun findOrCreateUser(
        userId: String,
        deviceId: String,
        displayName: String?,
    ): Pair<UserDto, Boolean> {
        val existing = userRepository.findById(userId)
        if (existing != null) {
            return existing to false
        }
        require(!displayName.isNullOrBlank()) { "Display name is required for new users" }
        val newUser = UserDto(
            deviceId = deviceId,
            profile = ProfileDto(displayName = displayName),
        )
        userRepository.save(userId, newUser)
        return newUser to true
    }

    override suspend fun findUser(userId: String): UserDto? =
        userRepository.findById(userId)

    override suspend fun updateUser(userId: String, incoming: UserDto): UserDto {
        val existing = userRepository.findById(userId) ?: incoming
        val merged = mergeStrategy.merge(existing, incoming)
        userRepository.save(userId, merged)
        return merged
    }

    override suspend fun deleteUser(userId: String) {
        userRepository.delete(userId)
    }
}
