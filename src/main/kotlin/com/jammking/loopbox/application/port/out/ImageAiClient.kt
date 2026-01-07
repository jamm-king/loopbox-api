package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider

interface ImageAiClient {

    val provider: ImageAiProvider

    fun generate(command: GenerateCommand): GenerateResult

    data class GenerateCommand(
        val config: ImageConfig
    )

    data class GenerateResult(
        val externalId: ExternalId
    )
}
