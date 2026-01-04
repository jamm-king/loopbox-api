package com.jammking.loopbox.adapter.out.router

import com.jammking.loopbox.application.port.out.MusicAiClient
import com.jammking.loopbox.application.port.out.MusicAiRouter
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import org.springframework.stereotype.Component

@Component
class MusicAiRouterImpl(
    clients: List<MusicAiClient>
): MusicAiRouter {

    private val clientMap: Map<MusicAiProvider, MusicAiClient> =
        clients.associateBy { it.provider }

    override fun getClient(provider: MusicAiProvider): MusicAiClient =
        clientMap[provider]
            ?: throw IllegalArgumentException("No MusicAiClient registered for provider=$provider")
}
