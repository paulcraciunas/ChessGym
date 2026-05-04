package com.paulcraciunas.chessgym.plugins

import com.paulcraciunas.chessgym.models.ErrorCode
import com.paulcraciunas.chessgym.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    message = cause.message ?: "Bad request",
                    code = ErrorCode.BAD_REQUEST,
                ),
            )
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    message = "Internal server error",
                    code = ErrorCode.INTERNAL_ERROR,
                ),
            )
        }
    }
}
