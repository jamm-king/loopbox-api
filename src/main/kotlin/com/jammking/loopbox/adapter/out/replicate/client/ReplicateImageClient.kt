package com.jammking.loopbox.adapter.out.replicate.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.out.replicate.dto.request.ReplicateImageInput
import com.jammking.loopbox.adapter.out.replicate.dto.request.ReplicatePredictionRequest
import com.jammking.loopbox.adapter.out.replicate.dto.response.ReplicatePredictionResponse
import com.jammking.loopbox.application.exception.ImageAiClientException
import com.jammking.loopbox.application.port.out.ImageAiClient
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ReplicateImageClient(
    @Value("\${loopbox.replicate.api-base-url:https://api.replicate.com}")
    private val apiBaseUrl: String,
    @Value("\${loopbox.replicate.api-token:}")
    private val apiToken: String,
    @Value("\${loopbox.replicate.callback-url:}")
    private val callbackUrl: String,
    @Value("\${loopbox.replicate.model:google/imagen-4}")
    private val model: String,
    private val objectMapper: ObjectMapper,
    private val client: OkHttpClient = OkHttpClient()
): ImageAiClient {

    private val log = LoggerFactory.getLogger(javaClass)

    override val provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4

    override fun generate(command: ImageAiClient.GenerateCommand): ImageAiClient.GenerateResult {
        val input = toReplicateInput(command.config)
        val requestBodyObj = ReplicatePredictionRequest(
            input = input,
            webhook = callbackUrl.ifBlank { null },
            webhookEventsFilter = if(callbackUrl.isBlank()) null else listOf("completed", "failed")
        )

        val json = try {
            objectMapper.writeValueAsString(requestBodyObj)
        } catch(e: JsonProcessingException) {
            throw ImageAiClientException.invalidJson(provider)
        }

        val baseUrl = apiBaseUrl.trimEnd('/')
        val requestUrl = "$baseUrl/v1/models/$model/predictions"

        val request = Request.Builder()
            .url(requestUrl)
            .header("Authorization", "Token $apiToken")
            .header("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = try {
                    response.body?.string()
                } catch(e: IOException) {
                    throw ImageAiClientException.invalidJson(provider)
                } ?: throw ImageAiClientException.emptyResponseBody(provider)

                if (!response.isSuccessful) {
                    log.error("Replicate prediction error: HTTP {}, body={}", response.code, body)
                    throw ImageAiClientException.invalidHttpCode(provider, response.code)
                }

                log.debug("Replicate prediction raw response: {}", body)

                val prediction = try {
                    objectMapper.readValue(body, ReplicatePredictionResponse::class.java)
                } catch(e: JsonProcessingException) {
                    throw ImageAiClientException.invalidJson(provider)
                }

                val taskId = prediction.id
                    ?: throw ImageAiClientException.missingTaskId(provider)

                return ImageAiClient.GenerateResult(
                    externalId = ExternalId(taskId)
                )
            }
        } catch(e: IOException) {
            throw ImageAiClientException.unknown(provider)
        }
    }

    private fun toReplicateInput(config: ImageConfig): ReplicateImageInput {
        val prompt = config.description?.takeIf { it.isNotBlank() }
            ?: throw ImageAiClientException.invalidPayloadState(provider)

        return ReplicateImageInput(
            prompt = prompt,
            aspectRatio = resolveAspectRatio(config),
            outputFormat = "jpg",
            safetyFilterLevel = "block_only_high"
        )
    }

    private fun resolveAspectRatio(config: ImageConfig): String {
        val width = config.width
        val height = config.height
        if (width == null || height == null || width <= 0 || height <= 0) return "1:1"
        if (width == height) return "1:1"
        if (width * 9 == height * 16) return "16:9"
        if (width * 16 == height * 9) return "9:16"
        if (width * 3 == height * 4) return "4:3"
        if (width * 4 == height * 3) return "3:4"
        return "1:1"
    }
}
