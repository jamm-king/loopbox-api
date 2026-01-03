package com.jammking.loopbox.adapter.out.suno.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.application.port.out.MusicAiClient
import com.jammking.loopbox.adapter.out.suno.dto.request.SunoGenerateRequest
import com.jammking.loopbox.adapter.out.suno.dto.response.SunoGenerateResponse
import com.jammking.loopbox.adapter.out.suno.mapper.SunoMapper.toPrompt
import com.jammking.loopbox.application.exception.MusicAiClientException
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class SunoMusicClient(
    @Value("\${loopbox.suno.api-base-url}")
    private val apiBaseUrl: String,
    @Value("\${loopbox.suno.api-key}")
    private val apiKey: String,
    @Value("\${loopbox.suno.callback-url}")
    private val callbackUrl: String,
    @Value("\${loopbox.suno.model}")
    private val model: String,
    private val objectMapper: ObjectMapper,
    private val client: OkHttpClient = OkHttpClient()
): MusicAiClient {

    private val log = LoggerFactory.getLogger(javaClass)

    override val provider = MusicAiProvider.SUNO

    override fun generate(
        command: MusicAiClient.GenerateCommand
    ): MusicAiClient.GenerateResult {

        val title = command.title
        val config = command.config
        val prompt = config.toPrompt(title)

        log.info("Requesting Suno generation: title='$title', prompt='$prompt'")

        val externalTaskId = callSuno(prompt)

        log.info("Requested Suno generation: externalTaskId=$externalTaskId")

        return MusicAiClient.GenerateResult(
            externalId = ExternalId(externalTaskId)
        )
    }

    override fun expand(command: MusicAiClient.ExpandCommand): MusicAiClient.ExpandResult {
        TODO("Not yet implemented")
    }

    private fun callSuno(prompt: String): String {

        val requestBodyObj = SunoGenerateRequest(
            prompt = prompt,
            style = null,
            title = null,
            customMode = false,
            instrumental = true,
            personaId = null,
            model = model,
            negativeTags = null,
            vocalGender = null,
            styleWeight = null,
            weirdnessConstraint = 0.5,
            audioWeight = null,
            callBackUrl = callbackUrl
        )

        val json = try {
            objectMapper.writeValueAsString(requestBodyObj)
        } catch(e: JsonProcessingException) {
            throw MusicAiClientException.invalidJson(provider)
        }

        val request = Request.Builder()
            .url("$apiBaseUrl/api/v1/generate")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = try {
                    response.body?.string()
                } catch(e: IOException) {
                    throw MusicAiClientException.invalidJson(provider)
                } ?: throw MusicAiClientException.emptyResponseBody(provider)

                if (!response.isSuccessful) {
                    log.error("Suno generate error: HTTP ${response.code}, body=$body")
                    throw MusicAiClientException.invalidHttpCode(provider, response.code)
                }

                log.debug("Suno generate raw response: $body")

                val genResponse = try {
                    objectMapper.readValue(body, SunoGenerateResponse::class.java)
                } catch(e: JsonProcessingException) {
                    throw MusicAiClientException.invalidJson(provider)
                }

                if (genResponse.code != 200) {
                    throw MusicAiClientException.invalidSystemCode(provider, genResponse.code)
                }

                val taskId = genResponse.data?.taskId
                    ?: throw MusicAiClientException.missingTaskId(provider)

                return taskId
            }
        } catch(e: IOException) {
            throw MusicAiClientException.unknown(provider)
        }
    }
}