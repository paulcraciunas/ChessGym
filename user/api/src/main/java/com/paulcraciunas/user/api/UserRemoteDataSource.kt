package com.paulcraciunas.user.api

interface UserRemoteDataSource {
    suspend fun getUser(userId: String): User
    suspend fun updateUser(user: User)

    suspend fun signIn(auth: User.AuthenticationState): User
    suspend fun deleteUser(userId: String)
}

class UserApiException(message: String, val statusCode: Int) : Exception(message)