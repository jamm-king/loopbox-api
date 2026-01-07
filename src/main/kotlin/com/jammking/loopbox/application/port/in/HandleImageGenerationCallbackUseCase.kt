package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.application.exception.ImageAiClientException
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider

interface HandleImageGenerationCallbackUseCase {

    fun handle(command: Command)

    data class Command(
        val provider: ImageAiProvider,
        val status: Status,
        val externalId: ExternalId,
        val images: List<ImageAsset>?,
        val message: String? = null
    ) {
        enum class Status { COMPLETED, FAILED, GENERATING, UNKNOWN }
        data class ImageAsset(
            val remoteUrl: String
        )

        fun validateProtocol() {
            if(status == Status.COMPLETED && images == null)
                throw ImageAiClientException.invalidPayloadState(provider)
            if(status == Status.FAILED && images != null)
                throw ImageAiClientException.invalidPayloadState(provider)
        }
    }
}
