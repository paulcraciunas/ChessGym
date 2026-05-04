package com.paulcraciunas.chessgym

import com.paulcraciunas.chessgym.config.FirebaseConfig
import com.paulcraciunas.chessgym.plugins.*
import com.paulcraciunas.chessgym.repositories.FirestoreUserRepository
import com.paulcraciunas.chessgym.services.DefaultStatisticsService
import com.paulcraciunas.chessgym.services.DefaultUserService
import com.paulcraciunas.chessgym.services.FirebaseAuthService
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Used in application.conf
fun Application.module() {
    val firestore = FirebaseConfig.initialize(environment)

    val authService = FirebaseAuthService()
    val userRepository = FirestoreUserRepository(firestore)
    val userService = DefaultUserService(userRepository)
    val statisticsService = DefaultStatisticsService(userRepository)

    configureSerialization()
    configureCallLogging()
    configureStatusPages()
    configureAuthentication(authService)
    configureRouting(userService, statisticsService)
    configureOpenApi()
}
