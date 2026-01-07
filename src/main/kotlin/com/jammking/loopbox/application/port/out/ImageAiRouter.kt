package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.task.ImageAiProvider

interface ImageAiRouter {
    fun getClient(provider: ImageAiProvider): ImageAiClient
}
