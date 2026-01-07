package com.jammking.loopbox.adapter.`in`.web.support

import com.jammking.loopbox.adapter.`in`.web.dto.imagen.callback.ImagenWebhookPayload
import com.jammking.loopbox.application.exception.ImageAiClientException
import com.jammking.loopbox.application.port.`in`.HandleImageGenerationCallbackUseCase
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ImagenWebhookNormalizer {

    private val log = LoggerFactory.getLogger(javaClass)
    private val provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4

    fun normalize(payload: ImagenWebhookPayload): HandleImageGenerationCallbackUseCase.Command {
        val taskId = payload.id?.trim()
        if (taskId.isNullOrEmpty()) {
            log.warn("Cannot normalize imagen webhook payload: id is missing")
            throw ImageAiClientException.invalidSchema(provider)
        }

        val status = decideStatus(payload)
        val images = buildImages(payload)

        val command = HandleImageGenerationCallbackUseCase.Command(
            provider = provider,
            status = status,
            externalId = ExternalId(taskId),
            images = images,
            message = payload.error
        )

        command.validateProtocol()
        return command
    }

    private fun decideStatus(payload: ImagenWebhookPayload): HandleImageGenerationCallbackUseCase.Command.Status {
        val status = payload.status?.lowercase()
        val hasImage = !payload.output.isNullOrBlank()

        return when(status) {
            "succeeded", "completed" ->
                if (hasImage) HandleImageGenerationCallbackUseCase.Command.Status.COMPLETED
                else HandleImageGenerationCallbackUseCase.Command.Status.UNKNOWN
            "failed", "canceled", "cancelled" -> HandleImageGenerationCallbackUseCase.Command.Status.FAILED
            "starting", "processing" -> HandleImageGenerationCallbackUseCase.Command.Status.GENERATING
            else -> HandleImageGenerationCallbackUseCase.Command.Status.UNKNOWN
        }
    }

    private fun buildImages(payload: ImagenWebhookPayload): List<HandleImageGenerationCallbackUseCase.Command.ImageAsset>? {
        val url = payload.output?.trim().orEmpty()
        if (url.isBlank()) return null

        return listOf(
            HandleImageGenerationCallbackUseCase.Command.ImageAsset(
                remoteUrl = url
            )
        )
    }
}
