package com.paulcraciunas.chessgym.plugins

import com.paulcraciunas.chessgym.routes.authRoutes
import com.paulcraciunas.chessgym.routes.healthRoutes
import com.paulcraciunas.chessgym.routes.statisticsRoutes
import com.paulcraciunas.chessgym.routes.userRoutes
import com.paulcraciunas.chessgym.services.StatisticsService
import com.paulcraciunas.chessgym.services.UserService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userService: UserService,
    statisticsService: StatisticsService,
) {
    routing {
        healthRoutes()
        authRoutes(userService)
        userRoutes(userService)
        statisticsRoutes(statisticsService)
    }
}
