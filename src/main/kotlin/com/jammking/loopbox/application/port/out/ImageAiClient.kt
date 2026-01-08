package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider

interface ImageAiClient {

    val provider: ImageAiProvider

    fun generate(command: GenerateCommand): GenerateResult

    fun fetchResult(command: FetchResultCommand): FetchResult

    data class GenerateCommand(
        val config: ImageConfig
    )

    data class GenerateResult(
        val externalId: ExternalId
    )

    data class FetchResultCommand(
        val externalId: ExternalId
    )

    data class FetchResult(
        val status: FetchStatus,
        val images: List<ImageAsset>?,
        val message: String? = null
    ) {
        enum class FetchStatus { COMPLETED, FAILED, GENERATING, UNKNOWN }
    }

    data class ImageAsset(
        val remoteUrl: String
    )
}
