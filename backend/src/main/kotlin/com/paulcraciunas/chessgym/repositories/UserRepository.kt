package com.paulcraciunas.chessgym.repositories

import com.paulcraciunas.chessgym.models.UserDto

interface UserRepository {
    suspend fun findById(userId: String): UserDto?
    suspend fun save(userId: String, user: UserDto)
    suspend fun delete(userId: String)
    suspend fun findAll(): List<UserDto>
}
