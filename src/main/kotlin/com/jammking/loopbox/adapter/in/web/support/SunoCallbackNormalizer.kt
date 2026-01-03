package com.jammking.loopbox.adapter.`in`.web.support

import com.jammking.loopbox.adapter.`in`.web.dto.suno.callback.SunoCallbackPayload
import com.jammking.loopbox.adapter.`in`.web.dto.suno.callback.SunoCallbackTrack
import com.jammking.loopbox.adapter.`in`.web.dto.suno.callback.SunoCallbackType
import com.jammking.loopbox.adapter.`in`.web.dto.suno.callback.SunoCode
import com.jammking.loopbox.application.exception.MusicAiClientException
import com.jammking.loopbox.application.port.`in`.HandleMusicGenerationCallbackUseCase
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SunoCallbackNormalizer {

    private val log = LoggerFactory.getLogger(javaClass)
    private val provider = MusicAiProvider.SUNO

    fun normalize(payload: SunoCallbackPayload): HandleMusicGenerationCallbackUseCase.Command {
        val data = payload.data
        if(data == null) {
            log.warn("Cannot normalize SUNO callback payload: SunoCallbackData is missing")
            throw MusicAiClientException.invalidSchema(provider)
        }

        val externalId = ExternalId(data.taskId)
        val code = SunoCode.from(payload.code)
        val callbackType = SunoCallbackType.from(data.callbackType)

        val tracks = buildValidTracks(data.data)

        val status = decideStatus(
            code = code,
            callbackType = callbackType,
            hasValidTracks = !tracks.isNullOrEmpty()
        )

        val command = HandleMusicGenerationCallbackUseCase.Command(
            provider = provider,
            status = status,
            externalId = externalId,
            tracks = tracks,
            message = payload.msg
        )

        command.validateProtocol()
        return command
    }

    private fun decideStatus(
        code: SunoCode?,
        callbackType: SunoCallbackType?,
        hasValidTracks: Boolean
    ): HandleMusicGenerationCallbackUseCase.Command.Status {

        if(code == SunoCode.BAD_REQUEST || code == SunoCode.DOWNLOAD_FAILED || code == SunoCode.SERVER_ERROR) {
            return HandleMusicGenerationCallbackUseCase.Command.Status.FAILED
        }

        return when(callbackType) {
            SunoCallbackType.ERROR -> HandleMusicGenerationCallbackUseCase.Command.Status.FAILED

            SunoCallbackType.COMPLETE ->
                if(code == SunoCode.SUCCESS && hasValidTracks)
                    HandleMusicGenerationCallbackUseCase.Command.Status.COMPLETED
                else
                    HandleMusicGenerationCallbackUseCase.Command.Status.UNKNOWN

            SunoCallbackType.FIRST -> HandleMusicGenerationCallbackUseCase.Command.Status.GENERATING
            SunoCallbackType.TEXT -> HandleMusicGenerationCallbackUseCase.Command.Status.GENERATING
            else -> HandleMusicGenerationCallbackUseCase.Command.Status.UNKNOWN
        }
    }

    private fun buildValidTracks(raw: List<SunoCallbackTrack>?): List<HandleMusicGenerationCallbackUseCase.Command.Track>? {
        if(raw.isNullOrEmpty()) return null

        val tracks = raw.mapNotNull { t ->
            val url = t.audioUrl?.trim().orEmpty()
            val durationSeconds = t.duration?.toInt() ?: 0
            val prompt = t.prompt?.trim()

            if(url.isBlank()) return@mapNotNull null
            if(durationSeconds <= 0) return@mapNotNull null
            if(prompt.isNullOrEmpty()) return@mapNotNull null

            HandleMusicGenerationCallbackUseCase.Command.Track(
                prompt = prompt,
                remoteUrl = url,
                durationSeconds = durationSeconds
            )
        }

        return tracks.takeIf { it.isNotEmpty() }
    }
}