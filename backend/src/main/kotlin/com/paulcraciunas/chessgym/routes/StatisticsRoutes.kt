package com.paulcraciunas.chessgym.routes

import com.paulcraciunas.chessgym.plugins.AUTH_FIREBASE
import com.paulcraciunas.chessgym.plugins.GLOBAL_RATE_LIMIT
import com.paulcraciunas.chessgym.services.StatisticsService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.statisticsRoutes(statisticsService: StatisticsService) {
    route("/api/v1/statistics") {
        rateLimit(GLOBAL_RATE_LIMIT) {
            authenticate(AUTH_FIREBASE) {
                get("/achievements") {
                    val stats = statisticsService.computeAchievementStatistics()
                    call.respond(HttpStatusCode.OK, stats)
                }
            }
        }
    }
}
