package com.jammking.loopbox.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import com.jammking.loopbox.adapter.`in`.web.dto.suno.callback.SunoCallbackData
import com.jammking.loopbox.adapter.`in`.web.dto.suno.callback.SunoCallbackPayload
import com.jammking.loopbox.adapter.`in`.web.support.SunoCallbackNormalizer
import com.jammking.loopbox.application.port.`in`.HandleMusicGenerationCallbackUseCase
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SunoCallbackController::class)
class SunoCallbackControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var useCase: HandleMusicGenerationCallbackUseCase

    @MockitoBean
    private lateinit var normalizer: SunoCallbackNormalizer

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `handleCallback should normalize payload and call usecase`() {
        // Given
        val payload = SunoCallbackPayload(
            code = 0,
            msg = "ok",
            data = SunoCallbackData(
                callbackType = "complete",
                taskId = "task-1",
                data = null
            )
        )
        val command = HandleMusicGenerationCallbackUseCase.Command(
            provider = MusicAiProvider.SUNO,
            status = HandleMusicGenerationCallbackUseCase.Command.Status.GENERATING,
            externalId = ExternalId("task-1"),
            tracks = null,
            message = "ok"
        )
        whenever(normalizer.normalize(any())).thenReturn(command)

        // When & Then
        mockMvc.perform(
            post("/api/internal/suno/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
        )
            .andExpect(status().isOk)

        verify(useCase).handle(command)
    }
}
