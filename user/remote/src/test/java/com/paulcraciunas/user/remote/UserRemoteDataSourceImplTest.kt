package com.paulcraciunas.user.remote

import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserApiException
import com.paulcraciunas.user.remote.mapper.UserDtoMapper
import com.paulcraciunas.user.remote.model.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UserRemoteDataSourceImplTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val mapper = UserDtoMapper()
    private val baseUrl = "http://localhost:8080"
    private val api = NetworkApi(baseUrl)
    private val testDispatcher = Dispatchers.Unconfined

    private fun createClient(engine: MockEngine): HttpClient = HttpClient(engine) {
        defaultRequest { contentType(ContentType.Application.Json) }
        install(ContentNegotiation) { json(json) }
    }

    @Test
    fun `GIVEN valid token WHEN signIn THEN POSTs to auth endpoint and returns user`() = runBlocking {
        // Given
        val responseDto = UserDto()
        val engine = MockEngine { request ->
            assertEquals("$baseUrl/api/v1/auth/signin", request.url.toString())
            assertEquals(HttpMethod.Post, request.method)
            respond(
                content = json.encodeToString(responseDto),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val underTest = UserRemoteDataSourceImpl(createClient(engine), mapper, api, testDispatcher)
        val auth = User.AuthenticationState(
            provider = User.AuthenticationState.AuthProvider.GOOGLE,
            userId = "uid_123",
        )

        // When
        val result = underTest.signIn(auth)

        // Then
        assertEquals(auth, result.authentication)
        assertEquals("ChessEnthusiast", result.profile.displayName)
    }

    @Test
    fun `GIVEN valid userId WHEN getUser THEN GETs from users endpoint`() = runBlocking {
        // Given
        val responseDto = UserDto()
        val engine = MockEngine { request ->
            assertEquals("$baseUrl/api/v1/users/uid_123", request.url.toString())
            assertEquals(HttpMethod.Get, request.method)
            respond(
                content = json.encodeToString(responseDto),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val underTest = UserRemoteDataSourceImpl(createClient(engine), mapper, api, testDispatcher)

        // When
        val result = underTest.getUser("uid_123")

        // Then
        assertEquals("ChessEnthusiast", result.profile.displayName)
        assertNull(result.authentication)
    }

    @Test
    fun `GIVEN authenticated user WHEN updateUser THEN PUTs to users endpoint`() = runBlocking {
        // Given
        val engine = MockEngine { request ->
            assertEquals("$baseUrl/api/v1/users/uid_456", request.url.toString())
            assertEquals(HttpMethod.Put, request.method)
            respond(
                content = json.encodeToString(UserDto()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val underTest = UserRemoteDataSourceImpl(createClient(engine), mapper, api, testDispatcher)
        val user = User(
            authentication = User.AuthenticationState(
                provider = User.AuthenticationState.AuthProvider.GOOGLE,
                userId = "uid_456",
            ),
        )

        // When & Then (no exception)
        underTest.updateUser(user)
    }

    @Test
    fun `GIVEN unauthenticated user WHEN updateUser THEN throws`() {
        // Given
        val engine = MockEngine { respond("", HttpStatusCode.OK) }
        val underTest = UserRemoteDataSourceImpl(createClient(engine), mapper, api, testDispatcher)
        val user = User()

        // When & Then
        assertThrows<IllegalStateException> {
            runBlocking { underTest.updateUser(user) }
        }
    }

    @Test
    fun `GIVEN server error WHEN updateUser THEN throws`() {
        // Given
        val engine = MockEngine {
            respond("Internal Server Error", HttpStatusCode.InternalServerError)
        }
        val underTest = UserRemoteDataSourceImpl(createClient(engine), mapper, api, testDispatcher)
        val user = User(
            authentication = User.AuthenticationState(
                provider = User.AuthenticationState.AuthProvider.GOOGLE,
                userId = "uid_789",
            ),
        )

        // When & Then
        assertThrows<UserApiException> {
            runBlocking { underTest.updateUser(user) }
        }
    }

    @Test
    fun `GIVEN valid userId WHEN deleteUser THEN DELETEs from users endpoint`() = runBlocking {
        // Given
        val engine = MockEngine { request ->
            assertEquals("$baseUrl/api/v1/users/uid_delete", request.url.toString())
            assertEquals(HttpMethod.Delete, request.method)
            respond("", HttpStatusCode.NoContent)
        }
        val underTest = UserRemoteDataSourceImpl(createClient(engine), mapper, api, testDispatcher)

        // When & Then (no exception)
        underTest.deleteUser("uid_delete")
    }

    @Test
    fun `GIVEN server error WHEN deleteUser THEN throws`() {
        // Given
        val engine = MockEngine {
            respond("Not Found", HttpStatusCode.NotFound)
        }
        val underTest = UserRemoteDataSourceImpl(createClient(engine), mapper, api, testDispatcher)

        // When & Then
        assertThrows<UserApiException> {
            runBlocking { underTest.deleteUser("uid_missing") }
        }
    }
}
