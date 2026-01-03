package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.application.exception.MusicAiClientException
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider

interface HandleMusicGenerationCallbackUseCase {

    fun handle(command: Command)

    data class Command(
        val provider: MusicAiProvider,
        val status: Status,
        val externalId: ExternalId,
        val tracks: List<Track>?,
        val message: String? = null
    ) {
        enum class Status { COMPLETED, FAILED, GENERATING, UNKNOWN }
        data class Track(
            val prompt: String,
            val remoteUrl: String,
            val durationSeconds: Int
        )

        fun validateProtocol() {
            if(status == Status.COMPLETED && tracks == null)
                throw MusicAiClientException.invalidPayloadState(provider)
            if(status == Status.FAILED && tracks != null)
                throw MusicAiClientException.invalidPayloadState(provider)
        }
    }
}