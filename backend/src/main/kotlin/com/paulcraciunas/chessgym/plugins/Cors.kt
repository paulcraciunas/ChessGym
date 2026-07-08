package com.paulcraciunas.chessgym.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        allowHost("chessgym.uk", schemes = listOf("https"))
        allowHost("www.chessgym.uk", schemes = listOf("https"))
        allowHost("chessgym-2c843.web.app", schemes = listOf("https"))

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }
}
