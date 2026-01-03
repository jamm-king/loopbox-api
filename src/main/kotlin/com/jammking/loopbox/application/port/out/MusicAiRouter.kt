package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.task.MusicAiProvider

interface MusicAiRouter {
    fun getClient(provider: MusicAiProvider): MusicAiClient
}