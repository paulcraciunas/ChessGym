package com.paulcraciunas.user.remote.client

import com.paulcraciunas.user.api.TokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    private const val TIMEOUT_MILLIS = 30_000L

    fun create(baseUrl: String, logger: Logger, tokenProvider: TokenProvider, isDebug: Boolean = false): HttpClient =
        HttpClient(OkHttp) {
            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MILLIS
                connectTimeoutMillis = TIMEOUT_MILLIS
                socketTimeoutMillis = TIMEOUT_MILLIS
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    isLenient = true
                })
            }

            install(Logging) {
                this.logger = logger
                level = if (isDebug) LogLevel.HEADERS else LogLevel.NONE
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        tokenProvider.getToken()?.let { BearerTokens(it, "") }
                    }
                    refreshTokens {
                        tokenProvider.getToken(forceRefresh = true)?.let { BearerTokens(it, "") }
                    }
                    sendWithoutRequest { request ->
                        request.url.buildString().startsWith(baseUrl)
                    }
                }
            }
        }
}
