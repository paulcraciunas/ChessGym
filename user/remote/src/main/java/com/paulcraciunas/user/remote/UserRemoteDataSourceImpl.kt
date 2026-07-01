package com.paulcraciunas.user.remote

import com.paulcraciunas.global.qualifiers.IoDispatcher
import com.paulcraciunas.user.api.AuthResult
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserApiException
import com.paulcraciunas.user.api.UserRemoteDataSource
import com.paulcraciunas.user.remote.mapper.UserDtoMapper
import com.paulcraciunas.user.remote.model.SignInRequest
import com.paulcraciunas.user.remote.model.SignInResponse
import com.paulcraciunas.user.remote.model.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val mapper: UserDtoMapper,
    private val api: NetworkApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : UserRemoteDataSource {

    override suspend fun signIn(authResult: AuthResult, deviceId: String): User = withContext(ioDispatcher) {
        val response: SignInResponse = httpClient.post(api.v1.signIn) {
            setBody(SignInRequest(
                deviceId = deviceId,
                displayName = authResult.displayName,
            ))
        }.safeBody()
        mapper.fromDto(response.user).copy(authentication = authResult.authState)
    }

    override suspend fun getUser(userId: String): User = withContext(ioDispatcher) {
        val apiUser: UserDto = httpClient.get(api.v1.user(userId))
            .safeBody()
        mapper.fromDto(apiUser)
    }

    override suspend fun updateUser(user: User): Unit = withContext(ioDispatcher) {
        val userId = user.authentication?.userId ?: throw IllegalStateException("Missing user authentication")
        val response = httpClient.put(api.v1.user(userId)) {
            setBody(mapper.toDto(user))
        }
        response.ensureSuccess("update")
    }

    override suspend fun deleteUser(userId: String): Unit = withContext(ioDispatcher) {
        val response = httpClient.delete(api.v1.user(userId))
        response.ensureSuccess("delete")
    }

    private suspend inline fun <reified T> HttpResponse.safeBody(): T {
        if (!status.isSuccess()) {
            throw UserApiException("API request failed with status: $status", status.value)
        }
        return body()
    }

    private fun HttpResponse.ensureSuccess(action: String) {
        if (!status.isSuccess()) {
            throw UserApiException("Failed to $action user: $status", status.value)
        }
    }
}
