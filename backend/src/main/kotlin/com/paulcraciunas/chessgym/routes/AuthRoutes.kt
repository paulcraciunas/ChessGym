package com.paulcraciunas.chessgym.routes

import com.paulcraciunas.chessgym.models.ErrorCode
import com.paulcraciunas.chessgym.models.ErrorResponse
import com.paulcraciunas.chessgym.models.SignInRequest
import com.paulcraciunas.chessgym.models.SignInResponse
import com.paulcraciunas.chessgym.plugins.AUTH_FIREBASE
import com.paulcraciunas.chessgym.plugins.authenticatedUid
import com.paulcraciunas.chessgym.services.UserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userService: UserService) {
    route("/api/v1/auth") {
        authenticate(AUTH_FIREBASE) {
            post("/signin") {
                val uid = call.authenticatedUid() ?: run {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", ErrorCode.UNAUTHORIZED),
                    )
                    return@post
                }

                val request = call.receive<SignInRequest>()
                val (user, isNew) = try {
                    userService.findOrCreateUser(uid, request.deviceId, request.displayName)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            e.message ?: "Invalid request",
                            ErrorCode.BAD_REQUEST,
                        ),
                    )
                    return@post
                }

                call.respond(
                    if (isNew) HttpStatusCode.Created else HttpStatusCode.OK,
                    SignInResponse(
                        userId = uid,
                        user = user,
                        isNewUser = isNew,
                    ),
                )
            }
        }
    }
}
