package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.suno.callback.SunoCallbackPayload
import com.jammking.loopbox.adapter.`in`.web.support.SunoCallbackNormalizer
import com.jammking.loopbox.application.exception.MusicAiClientException
import com.jammking.loopbox.application.port.`in`.HandleMusicGenerationCallbackUseCase
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/internal/suno")
class SunoCallbackController(
    private val useCase: HandleMusicGenerationCallbackUseCase,
    private val normalizer: SunoCallbackNormalizer
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val provider = MusicAiProvider.SUNO

    @PostMapping("/callback")
    fun handleCallback(
        @RequestBody payload: SunoCallbackPayload
    ): ResponseEntity<Unit> {
        log.info("Received Suno callback payload: {}", payload)

        val data = payload.data ?: throw MusicAiClientException.invalidSchema(provider)
        if(data.taskId.isBlank()) throw MusicAiClientException.missingTaskId(provider)
        if(data.callbackType.isBlank()) throw MusicAiClientException.invalidSchema(provider)

        val command = normalizer.normalize(payload)
        useCase.handle(command)
        return ResponseEntity.ok().build()
    }
}