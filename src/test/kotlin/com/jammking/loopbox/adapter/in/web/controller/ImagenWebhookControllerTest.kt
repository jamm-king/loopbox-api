package com.jammking.loopbox.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import com.jammking.loopbox.adapter.`in`.web.dto.imagen.callback.ImagenWebhookPayload
import com.jammking.loopbox.adapter.`in`.web.support.ImagenWebhookNormalizer
import com.jammking.loopbox.application.port.`in`.HandleImageGenerationCallbackUseCase
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
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

@WebMvcTest(ImagenWebhookController::class)
class ImagenWebhookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var useCase: HandleImageGenerationCallbackUseCase

    @MockitoBean
    private lateinit var normalizer: ImagenWebhookNormalizer

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `handleCallback should normalize payload and call usecase`() {
        val payload = ImagenWebhookPayload(
            id = "task-1",
            status = "processing",
            output = null,
            error = null
        )
        val command = HandleImageGenerationCallbackUseCase.Command(
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            status = HandleImageGenerationCallbackUseCase.Command.Status.GENERATING,
            externalId = ExternalId("task-1"),
            images = null,
            message = null
        )
        whenever(normalizer.normalize(any())).thenReturn(command)

        mockMvc.perform(
            post("/api/internal/imagen/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
        )
            .andExpect(status().isOk)

        verify(useCase).handle(command)
    }
}
