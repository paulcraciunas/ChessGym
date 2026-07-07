package com.paulcraciunas.chessgym.routes

import com.paulcraciunas.chessgym.models.ErrorCode
import com.paulcraciunas.chessgym.models.ErrorResponse
import com.paulcraciunas.chessgym.models.UserDto
import com.paulcraciunas.chessgym.plugins.AUTH_FIREBASE
import com.paulcraciunas.chessgym.plugins.GLOBAL_RATE_LIMIT
import com.paulcraciunas.chessgym.plugins.isAuthorized
import com.paulcraciunas.chessgym.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * A plugin that ensures the userId in the path matches the authenticated user.
 */
private val UserAuthorizationPlugin = createRouteScopedPlugin("UserAuthorizationPlugin") {
    on(AuthenticationChecked) { call ->
        val userId = call.parameters["userId"] ?: run {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing userId", ErrorCode.BAD_REQUEST)
            )
            return@on
        }

        if (!call.isAuthorized(userId)) {
            // isAuthorized extension already calls call.respond(Forbidden)
            return@on
        }
    }
}

/**
 * Custom DSL builder that applies the authorization plugin and sets up the path.
 */
private fun Route.userAuthorizedRoute(build: Route.() -> Unit) {
    route("/{userId}") {
        install(UserAuthorizationPlugin)
        build()
    }
}

// Helper to get the userId safely in child routes
private val RoutingContext.userId: String get() = call.parameters["userId"]!!

fun Route.userRoutes(userService: UserService) {
    route("/api/v1/users") {
        rateLimit(GLOBAL_RATE_LIMIT) {
            authenticate(AUTH_FIREBASE) {
                userAuthorizedRoute {
                    get {
                        val user = userService.findUser(userId) ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("User not found", ErrorCode.NOT_FOUND),
                        )
                        call.respond(HttpStatusCode.OK, user)
                    }

                    put {
                        val incoming = call.receive<UserDto>()
                        val merged = userService.updateUser(userId, incoming)
                        call.respond(HttpStatusCode.OK, merged)
                    }

                    delete {
                        userService.deleteUser(userId)
                        call.respond(HttpStatusCode.NoContent)
                    }
                }
            }
        }
    }
}
