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
                callbackUrl = "https://callback",
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
            assertEquals("https://callback", request.webhook)
            assertEquals(listOf("completed"), request.webhookEventsFilter)
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

    @Test
    fun `fetchResult should map succeeded to completed`() {
        val response = """{"id":"task-123","status":"succeeded","output":["https://img.test/1.jpg"]}"""
        val server = startServer("/v1/predictions/task-123", 200, response)

        try {
            val client = createClient(server, "http://callback")
            val result = client.fetchResult(ImageAiClient.FetchResultCommand(ExternalId("task-123")))

            assertEquals(ImageAiClient.FetchResult.FetchStatus.COMPLETED, result.status)
            assertEquals("https://img.test/1.jpg", result.images?.first()?.remoteUrl)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `fetchResult should map succeeded without output to failed`() {
        val response = """{"id":"task-123","status":"succeeded"}"""
        val server = startServer("/v1/predictions/task-123", 200, response)

        try {
            val client = createClient(server, "http://callback")
            val result = client.fetchResult(ImageAiClient.FetchResultCommand(ExternalId("task-123")))

            assertEquals(ImageAiClient.FetchResult.FetchStatus.FAILED, result.status)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `fetchResult should map failed status`() {
        val response = """{"id":"task-123","status":"failed","error":"boom"}"""
        val server = startServer("/v1/predictions/task-123", 200, response)

        try {
            val client = createClient(server, "http://callback")
            val result = client.fetchResult(ImageAiClient.FetchResultCommand(ExternalId("task-123")))

            assertEquals(ImageAiClient.FetchResult.FetchStatus.FAILED, result.status)
            assertEquals("boom", result.message)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `fetchResult should map processing status to generating`() {
        val response = """{"id":"task-123","status":"processing"}"""
        val server = startServer("/v1/predictions/task-123", 200, response)

        try {
            val client = createClient(server, "http://callback")
            val result = client.fetchResult(ImageAiClient.FetchResultCommand(ExternalId("task-123")))

            assertEquals(ImageAiClient.FetchResult.FetchStatus.GENERATING, result.status)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `fetchResult should map unknown status to unknown`() {
        val response = """{"id":"task-123","status":"mystery"}"""
        val server = startServer("/v1/predictions/task-123", 200, response)

        try {
            val client = createClient(server, "http://callback")
            val result = client.fetchResult(ImageAiClient.FetchResultCommand(ExternalId("task-123")))

            assertEquals(ImageAiClient.FetchResult.FetchStatus.UNKNOWN, result.status)
        } finally {
            server.stop(0)
        }
    }

    private fun startServer(path: String, code: Int, response: String): HttpServer {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext(path) { exchange ->
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(code, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()
        return server
    }

    private fun createClient(server: HttpServer, callbackUrl: String): ReplicateImageClient =
        ReplicateImageClient(
            apiBaseUrl = "http://localhost:${server.address.port}",
            apiToken = "api-token",
            callbackUrl = callbackUrl,
            model = "google/imagen-4",
            objectMapper = objectMapper
        )
}
