package com.jammking.loopbox.adapter.out.router

import com.jammking.loopbox.application.port.out.ImageAiClient
import com.jammking.loopbox.application.port.out.ImageAiRouter
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import org.springframework.stereotype.Component

@Component
class ImageAiRouterImpl(
    clients: List<ImageAiClient>
): ImageAiRouter {

    private val clientMap: Map<ImageAiProvider, ImageAiClient> =
        clients.associateBy { it.provider }

    override fun getClient(provider: ImageAiProvider): ImageAiClient =
        clientMap[provider]
            ?: throw IllegalArgumentException("No ImageAiClient registered for provider=$provider")
}
