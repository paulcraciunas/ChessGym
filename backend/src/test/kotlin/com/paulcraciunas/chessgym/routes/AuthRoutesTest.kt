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
            setBody(SignInRequest(deviceId = "device-1"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<SignInResponse>()
        assertEquals("user-123", body.userId)
        assertTrue(body.isNewUser)
    }

    @Test
    fun `POST signin with existing user returns 200`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("valid-token", uid = "user-123", email = "test@example.com")

        client.post("/api/v1/auth/signin") {
            contentType(ContentType.Application.Json)
            bearerAuth("valid-token")
            setBody(SignInRequest(deviceId = "device-1"))
        }

        val response = client.post("/api/v1/auth/signin") {
            contentType(ContentType.Application.Json)
            bearerAuth("valid-token")
            setBody(SignInRequest(deviceId = "device-1"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<SignInResponse>()
        assertEquals("user-123", body.userId)
        assertTrue(!body.isNewUser)
    }
}
