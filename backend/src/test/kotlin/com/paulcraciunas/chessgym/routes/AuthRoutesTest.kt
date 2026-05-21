package com.paulcraciunas.chessgym.routes

import com.paulcraciunas.chessgym.helpers.FakeAuthService
import com.paulcraciunas.chessgym.helpers.InMemoryUserRepository
import com.paulcraciunas.chessgym.helpers.testApplication
import com.paulcraciunas.chessgym.models.SignInRequest
import com.paulcraciunas.chessgym.models.SignInResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {
    private val fakeAuth = FakeAuthService()
    private val repository = InMemoryUserRepository()

    @Test
    fun `POST signin without token returns 401`() = testApplication(fakeAuth, repository) { client ->
        val response = client.post("/api/v1/auth/signin") {
            contentType(ContentType.Application.Json)
            setBody(SignInRequest(deviceId = "device-1"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST signin with invalid token returns 401`() = testApplication(fakeAuth, repository) { client ->
        val response = client.post("/api/v1/auth/signin") {
            contentType(ContentType.Application.Json)
            bearerAuth("invalid-token")
            setBody(SignInRequest(deviceId = "device-1"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST signin with valid token creates new user`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("valid-token", uid = "user-123", email = "test@example.com")

        val response = client.post("/api/v1/auth/signin") {
            contentType(ContentType.Application.Json)
            bearerAuth("valid-token")
            setBody(SignInRequest(deviceId = "device-1", displayName = "ChessPlayer"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<SignInResponse>()
        assertEquals("user-123", body.userId)
        assertTrue(body.isNewUser)
        assertEquals("ChessPlayer", body.user.profile.displayName)
        assertEquals("device-1", body.user.deviceId)
    }

    @Test
    fun `POST signin without displayName for new user returns 400`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("valid-token", uid = "user-123", email = "test@example.com")

        val response = client.post("/api/v1/auth/signin") {
            contentType(ContentType.Application.Json)
            bearerAuth("valid-token")
            setBody(SignInRequest(deviceId = "device-1"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST signin with blank displayName for new user returns 400`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("valid-token", uid = "user-123", email = "test@example.com")

        val response = client.post("/api/v1/auth/signin") {
            contentType(ContentType.Application.Json)
            bearerAuth("valid-token")
            setBody(SignInRequest(deviceId = "device-1", displayName = "   "))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST signin with existing user returns 200 and preserves original data`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("valid-token", uid = "user-123", email = "test@example.com")

        client.post("/api/v1/auth/signin") {
            contentType(ContentType.Application.Json)
            bearerAuth("valid-token")
            setBody(SignInRequest(deviceId = "device-1", displayName = "ChessPlayer"))
        }

        val response = client.post("/api/v1/auth/signin") {
            contentType(ContentType.Application.Json)
            bearerAuth("valid-token")
            setBody(SignInRequest(deviceId = "device-2", displayName = "NewName"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<SignInResponse>()
        assertEquals("user-123", body.userId)
        assertTrue(!body.isNewUser)
        assertEquals("device-1", body.user.deviceId)
        assertEquals("ChessPlayer", body.user.profile.displayName)
    }
}
