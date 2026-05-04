package com.paulcraciunas.chessgym.routes

import com.paulcraciunas.chessgym.services.StatisticsService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.statisticsRoutes(statisticsService: StatisticsService) {
    route("/api/v1/statistics") {
        get("/achievements") {
            val stats = statisticsService.computeAchievementStatistics()
            call.respond(HttpStatusCode.OK, stats)
        }
    }
}
