package com.paulcraciunas.chessgym.helpers

import com.paulcraciunas.chessgym.plugins.*
import com.paulcraciunas.chessgym.routes.authRoutes
import com.paulcraciunas.chessgym.routes.healthRoutes
import com.paulcraciunas.chessgym.routes.statisticsRoutes
import com.paulcraciunas.chessgym.routes.userRoutes
import com.paulcraciunas.chessgym.services.DefaultStatisticsService
import com.paulcraciunas.chessgym.services.DefaultUserService
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json

fun testApplication(
    fakeAuth: FakeAuthService = FakeAuthService(),
    repository: InMemoryUserRepository = InMemoryUserRepository(),
    block: suspend ApplicationTestBuilder.(HttpClient) -> Unit,
) {
    io.ktor.server.testing.testApplication {
        val userService = DefaultUserService(repository)
        val statisticsService = DefaultStatisticsService(repository, cacheTtlSeconds = 0)

        application {
            configureSerialization()
            configureStatusPages()
            configureRateLimiting()
            configureAuthentication(fakeAuth)
            routing {
                healthRoutes()
                authRoutes(userService)
                userRoutes(userService)
                statisticsRoutes(statisticsService)
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = false
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }

        block(client)
    }
}
