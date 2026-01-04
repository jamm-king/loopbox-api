package com.jammking.loopbox.adapter.out.suno.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.out.suno.dto.request.SunoGenerateRequest
import com.jammking.loopbox.application.exception.MusicAiClientException
import com.jammking.loopbox.application.exception.PortErrorCode
import com.jammking.loopbox.application.port.out.MusicAiClient
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.any
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference

class SunoMusicClientTest {

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @Test
    fun `generate should return external id when response is successful`() {
        val capturedAuth = AtomicReference<String?>()
        val capturedBody = AtomicReference<String?>()
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/api/v1/generate") { exchange ->
            capturedAuth.set(exchange.requestHeaders.getFirst("Authorization"))
            capturedBody.set(exchange.requestBody.readBytes().toString(Charsets.UTF_8))

            val response = """{"code":200,"msg":"ok","data":{"taskId":"task-123"}}"""
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()

        try {
            val client = SunoMusicClient(
                apiBaseUrl = "http://localhost:${server.address.port}",
                apiKey = "api-key",
                callbackUrl = "http://callback",
                model = "test-model",
                objectMapper = objectMapper
            )
            val result = client.generate(
                MusicAiClient.GenerateCommand(
                    title = "Focus",
                    config = MusicConfig(mood = "bright", bpm = 120)
                )
            )

            assertEquals(ExternalId("task-123"), result.externalId)
            assertEquals("Bearer api-key", capturedAuth.get())

            val request = objectMapper.readValue(
                capturedBody.get(),
                SunoGenerateRequest::class.java
            )
            assertEquals("test-model", request.model)
            assertEquals("http://callback", request.callBackUrl)
            assertEquals(true, request.instrumental)
            assertEquals(false, request.customMode)
            assertEquals(
                "Simple loop background music, title of \"Focus\", bright mood, 120 bpm",
                request.prompt
            )
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `generate should throw when http code is not successful`() {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/api/v1/generate") { exchange ->
            val response = """{"code":200,"msg":"ok","data":{"taskId":"task-123"}}"""
            exchange.sendResponseHeaders(500, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()

        try {
            val client = SunoMusicClient(
                apiBaseUrl = "http://localhost:${server.address.port}",
                apiKey = "api-key",
                callbackUrl = "http://callback",
                model = "test-model",
                objectMapper = objectMapper
            )

            val exception = assertThrows(MusicAiClientException::class.java) {
                client.generate(MusicAiClient.GenerateCommand("Focus", MusicConfig()))
            }

            assertEquals(PortErrorCode.TEMPORARY_UNAVAILABLE, exception.code)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `generate should throw when system code is invalid`() {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/api/v1/generate") { exchange ->
            val response = """{"code":500,"msg":"error","data":{"taskId":"task-123"}}"""
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()

        try {
            val client = SunoMusicClient(
                apiBaseUrl = "http://localhost:${server.address.port}",
                apiKey = "api-key",
                callbackUrl = "http://callback",
                model = "test-model",
                objectMapper = objectMapper
            )

            val exception = assertThrows(MusicAiClientException::class.java) {
                client.generate(MusicAiClient.GenerateCommand("Focus", MusicConfig()))
            }

            assertEquals(PortErrorCode.PROTOCOL_VIOLATION, exception.code)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `generate should throw when task id is missing`() {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/api/v1/generate") { exchange ->
            val response = """{"code":200,"msg":"ok","data":null}"""
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()

        try {
            val client = SunoMusicClient(
                apiBaseUrl = "http://localhost:${server.address.port}",
                apiKey = "api-key",
                callbackUrl = "http://callback",
                model = "test-model",
                objectMapper = objectMapper
            )

            val exception = assertThrows(MusicAiClientException::class.java) {
                client.generate(MusicAiClient.GenerateCommand("Focus", MusicConfig()))
            }

            assertEquals(PortErrorCode.PROTOCOL_VIOLATION, exception.code)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `generate should throw when response json is invalid`() {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/api/v1/generate") { exchange ->
            val response = "not-json"
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()

        try {
            val client = SunoMusicClient(
                apiBaseUrl = "http://localhost:${server.address.port}",
                apiKey = "api-key",
                callbackUrl = "http://callback",
                model = "test-model",
                objectMapper = objectMapper
            )

            val exception = assertThrows(MusicAiClientException::class.java) {
                client.generate(MusicAiClient.GenerateCommand("Focus", MusicConfig()))
            }

            assertEquals(PortErrorCode.PROTOCOL_VIOLATION, exception.code)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `generate should throw when request json is invalid`() {
        val brokenMapper = mock<ObjectMapper> {
            on { writeValueAsString(any()) } doThrow object : JsonProcessingException("boom", JsonLocation.NA) {}
        }
        val client = SunoMusicClient(
            apiBaseUrl = "http://localhost:0",
            apiKey = "api-key",
            callbackUrl = "http://callback",
            model = "test-model",
            objectMapper = brokenMapper
        )

        val exception = assertThrows(MusicAiClientException::class.java) {
            client.generate(MusicAiClient.GenerateCommand("Focus", MusicConfig()))
        }

        assertEquals(PortErrorCode.PROTOCOL_VIOLATION, exception.code)
    }
}
