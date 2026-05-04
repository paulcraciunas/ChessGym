package com.paulcraciunas.chessgym.helpers

import com.paulcraciunas.chessgym.models.UserDto
import com.paulcraciunas.chessgym.repositories.UserRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserRepository : UserRepository {
    private val store: MutableMap<String, UserDto> = ConcurrentHashMap()

    override suspend fun findById(userId: String): UserDto? = store[userId]

    override suspend fun save(userId: String, user: UserDto) {
        store[userId] = user
    }

    override suspend fun delete(userId: String) {
        store.remove(userId)
    }

    override suspend fun findAll(): List<UserDto> = store.values.toList()

    fun clear() = store.clear()
}
