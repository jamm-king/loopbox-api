package com.jammking.loopbox.adapter.out.replicate.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.out.replicate.dto.request.ReplicatePredictionRequest
import com.jammking.loopbox.application.exception.ImageAiClientException
import com.jammking.loopbox.application.exception.PortErrorCode
import com.jammking.loopbox.application.port.out.ImageAiClient
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference

class ReplicateImageClientTest {

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @Test
    fun `generate should return external id when response is successful`() {
        val capturedAuth = AtomicReference<String?>()
        val capturedBody = AtomicReference<String?>()
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/v1/models/google/imagen-4/predictions") { exchange ->
            capturedAuth.set(exchange.requestHeaders.getFirst("Authorization"))
            capturedBody.set(exchange.requestBody.readBytes().toString(Charsets.UTF_8))

            val response = """{"id":"task-123","status":"starting"}"""
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(201, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()

        try {
            val client = ReplicateImageClient(
                apiBaseUrl = "http://localhost:${server.address.port}",
                apiToken = "api-token",
                callbackUrl = "http://callback",
                model = "google/imagen-4",
                objectMapper = objectMapper
            )
            val result = client.generate(
                ImageAiClient.GenerateCommand(
                    config = ImageConfig(description = "A sunrise", width = 1600, height = 900)
                )
            )

            assertEquals(ExternalId("task-123"), result.externalId)
            assertEquals("Token api-token", capturedAuth.get())

            val request = objectMapper.readValue(
                capturedBody.get(),
                ReplicatePredictionRequest::class.java
            )
            assertEquals("A sunrise", request.input.prompt)
            assertEquals("16:9", request.input.aspectRatio)
            assertEquals("jpg", request.input.outputFormat)
            assertEquals("block_only_high", request.input.safetyFilterLevel)
            assertEquals("http://callback", request.webhook)
            assertEquals(listOf("completed", "failed"), request.webhookEventsFilter)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `generate should throw when http code is not successful`() {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/v1/models/google/imagen-4/predictions") { exchange ->
            val response = """{"id":"task-123","status":"starting"}"""
            exchange.sendResponseHeaders(500, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()

        try {
            val client = ReplicateImageClient(
                apiBaseUrl = "http://localhost:${server.address.port}",
                apiToken = "api-token",
                callbackUrl = "http://callback",
                model = "google/imagen-4",
                objectMapper = objectMapper
            )

            val exception = assertThrows(ImageAiClientException::class.java) {
                client.generate(ImageAiClient.GenerateCommand(ImageConfig(description = "A sunrise")))
            }

            assertEquals(PortErrorCode.TEMPORARY_UNAVAILABLE, exception.code)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `generate should throw when task id is missing`() {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/v1/models/google/imagen-4/predictions") { exchange ->
            val response = """{"status":"starting"}"""
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()

        try {
            val client = ReplicateImageClient(
                apiBaseUrl = "http://localhost:${server.address.port}",
                apiToken = "api-token",
                callbackUrl = "http://callback",
                model = "google/imagen-4",
                objectMapper = objectMapper
            )

            val exception = assertThrows(ImageAiClientException::class.java) {
                client.generate(ImageAiClient.GenerateCommand(ImageConfig(description = "A sunrise")))
            }

            assertEquals(PortErrorCode.PROTOCOL_VIOLATION, exception.code)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `generate should throw when response json is invalid`() {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/v1/models/google/imagen-4/predictions") { exchange ->
            val response = "not-json"
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()

        try {
            val client = ReplicateImageClient(
                apiBaseUrl = "http://localhost:${server.address.port}",
                apiToken = "api-token",
                callbackUrl = "http://callback",
                model = "google/imagen-4",
                objectMapper = objectMapper
            )

            val exception = assertThrows(ImageAiClientException::class.java) {
                client.generate(ImageAiClient.GenerateCommand(ImageConfig(description = "A sunrise")))
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
        val client = ReplicateImageClient(
            apiBaseUrl = "http://localhost:0",
            apiToken = "api-token",
            callbackUrl = "http://callback",
            model = "google/imagen-4",
            objectMapper = brokenMapper
        )

        val exception = assertThrows(ImageAiClientException::class.java) {
            client.generate(ImageAiClient.GenerateCommand(ImageConfig(description = "A sunrise")))
        }

        assertEquals(PortErrorCode.PROTOCOL_VIOLATION, exception.code)
    }
}
