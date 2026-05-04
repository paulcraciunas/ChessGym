package com.paulcraciunas.chessgym.services

import com.paulcraciunas.chessgym.models.UserDto

interface UserService {
    suspend fun findOrCreateUser(userId: String, deviceId: String): Pair<UserDto, Boolean>
    suspend fun findUser(userId: String): UserDto?
    suspend fun updateUser(userId: String, incoming: UserDto): UserDto
    suspend fun deleteUser(userId: String)
}
