package com.paulcraciunas.chessgym.plugins

import com.paulcraciunas.chessgym.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

const val AUTH_FIREBASE: String = "firebase"

private val authLogger = LoggerFactory.getLogger("Authentication")

data class FirebasePrincipal(
    val uid: String,
    val email: String?,
)

fun Application.configureAuthentication(authService: AuthService) {
    install(Authentication) {
        bearer(AUTH_FIREBASE) {
            authenticate { tokenCredential ->
                try {
                    val decoded = authService.verifyIdToken(tokenCredential.token)
                    FirebasePrincipal(
                        uid = decoded.uid,
                        email = decoded.email,
                    )
                } catch (e: Exception) {
                    authLogger.warn("Token verification failed: {}", e.message)
                    null
                }
            }
        }
    }
}

fun ApplicationCall.authenticatedUid(): String? =
    principal<FirebasePrincipal>()?.uid

suspend fun ApplicationCall.isAuthorized(userId: String): Boolean {
    val uid = authenticatedUid()
    if (uid == null || uid != userId) {
        respond(HttpStatusCode.Forbidden)
        return false
    }
    return true
}
