package com.paulcraciunas.chessgym.routes

import com.paulcraciunas.chessgym.helpers.testApplication
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthRoutesTest {
    @Test
    fun `GET health returns 200 with ok status`() = testApplication { client ->
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<Map<String, String>>()
        assertEquals("ok", body["status"])
    }
}
