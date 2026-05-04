package com.paulcraciunas.chessgym.routes

import com.paulcraciunas.chessgym.helpers.FakeAuthService
import com.paulcraciunas.chessgym.helpers.InMemoryUserRepository
import com.paulcraciunas.chessgym.helpers.testApplication
import com.paulcraciunas.chessgym.models.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRoutesTest {
    private val fakeAuth = FakeAuthService()
    private val repository = InMemoryUserRepository()

    @Test
    fun `GET user without auth returns 401`() = testApplication(fakeAuth, repository) { client ->
        val response = client.get("/api/v1/users/user-123")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET user for another user returns 403`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("token-a", uid = "user-a")

        val response = client.get("/api/v1/users/user-b") {
            bearerAuth("token-a")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET non-existent user returns 404`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("token-a", uid = "user-a")

        val response = client.get("/api/v1/users/user-a") {
            bearerAuth("token-a")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = response.body<ErrorResponse>()
        assertEquals(ErrorCode.NOT_FOUND, body.code)
        assertEquals("User not found", body.message)
    }

    @Test
    fun `PUT then GET returns merged user`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("token-a", uid = "user-a")

        val userDto = UserDto(
            profile = ProfileDto(
                firstName = "Paul",
                lastName = "C",
                lastModified = 1000L,
            ),
            ratings = RatingsDto(current = 1200, blindMode = 500),
            highScores = HighScoresDto(ratedPuzzle = 1250, puzzleRush = 15),
        )

        val putResponse = client.put("/api/v1/users/user-a") {
            contentType(ContentType.Application.Json)
            bearerAuth("token-a")
            setBody(userDto)
        }
        assertEquals(HttpStatusCode.OK, putResponse.status)

        val getResponse = client.get("/api/v1/users/user-a") {
            bearerAuth("token-a")
        }
        assertEquals(HttpStatusCode.OK, getResponse.status)

        val retrieved = getResponse.body<UserDto>()
        assertEquals("Paul", retrieved.profile.firstName)
        assertEquals(1200, retrieved.ratings.current)
        assertEquals(15, retrieved.highScores.puzzleRush)
    }

    @Test
    fun `PUT cannot modify another user`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("token-a", uid = "user-a")

        val response = client.put("/api/v1/users/user-b") {
            contentType(ContentType.Application.Json)
            bearerAuth("token-a")
            setBody(UserDto())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `DELETE removes user`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("token-a", uid = "user-a")

        client.put("/api/v1/users/user-a") {
            contentType(ContentType.Application.Json)
            bearerAuth("token-a")
            setBody(UserDto())
        }

        val deleteResponse = client.delete("/api/v1/users/user-a") {
            bearerAuth("token-a")
        }
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val getResponse = client.get("/api/v1/users/user-a") {
            bearerAuth("token-a")
        }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `DELETE cannot delete another user`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("token-a", uid = "user-a")

        val response = client.delete("/api/v1/users/user-b") {
            bearerAuth("token-a")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PUT twice merges values correctly`() = testApplication(fakeAuth, repository) { client ->
        fakeAuth.registerToken("token-a", uid = "user-a")

        val firstUpdate = UserDto(
            ratings = RatingsDto(current = 1200, blindMode = 500),
            statistics = StatisticsDto(puzzlesPlayed = 50),
        )
        client.put("/api/v1/users/user-a") {
            contentType(ContentType.Application.Json)
            bearerAuth("token-a")
            setBody(firstUpdate)
        }

        val secondUpdate = UserDto(
            ratings = RatingsDto(current = 1100, blindMode = 600),
            statistics = StatisticsDto(puzzlesPlayed = 75),
        )
        client.put("/api/v1/users/user-a") {
            contentType(ContentType.Application.Json)
            bearerAuth("token-a")
            setBody(secondUpdate)
        }

        val getResponse = client.get("/api/v1/users/user-a") {
            bearerAuth("token-a")
        }
        val user = getResponse.body<UserDto>()

        assertEquals(1200, user.ratings.current)
        assertEquals(600, user.ratings.blindMode)
        assertEquals(75, user.statistics.puzzlesPlayed)
    }
}
