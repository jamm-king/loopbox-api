package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.imagen.callback.ImagenWebhookPayload
import com.jammking.loopbox.adapter.`in`.web.support.ImagenWebhookNormalizer
import com.jammking.loopbox.application.exception.ImageAiClientException
import com.jammking.loopbox.application.port.`in`.HandleImageGenerationCallbackUseCase
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/internal/imagen")
class ImagenWebhookController(
    private val useCase: HandleImageGenerationCallbackUseCase,
    private val normalizer: ImagenWebhookNormalizer
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4

    @PostMapping("/callback")
    fun handleCallback(
        @RequestBody payload: ImagenWebhookPayload
    ): ResponseEntity<Unit> {
        log.info("Received Imagen callback payload: {}", payload)

        val taskId = payload.id?.trim().orEmpty()
        if(taskId.isBlank()) throw ImageAiClientException.missingTaskId(provider)

        val command = normalizer.normalize(payload)
        useCase.handle(command)
        return ResponseEntity.ok().build()
    }
}
