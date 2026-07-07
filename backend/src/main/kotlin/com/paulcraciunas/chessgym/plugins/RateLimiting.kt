package com.paulcraciunas.chessgym.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

/**
 * Rate limit scopes used in route definitions.
 *
 * [GLOBAL_RATE_LIMIT] — applied to all authenticated API endpoints (100 req/min per IP).
 * [AUTH_RATE_LIMIT] — applied specifically to auth endpoints (10 req/min per IP) to prevent brute-force.
 */
val GLOBAL_RATE_LIMIT = RateLimitName("global")
val AUTH_RATE_LIMIT = RateLimitName("auth")

fun Application.configureRateLimiting() {
    install(RateLimit) {
        register(GLOBAL_RATE_LIMIT) {
            rateLimiter(limit = 100, refillPeriod = 1.minutes)
            requestKey { call ->
                call.request.origin.remoteAddress
            }
        }
        register(AUTH_RATE_LIMIT) {
            rateLimiter(limit = 10, refillPeriod = 1.minutes)
            requestKey { call ->
                call.request.origin.remoteAddress
            }
        }
    }
}
