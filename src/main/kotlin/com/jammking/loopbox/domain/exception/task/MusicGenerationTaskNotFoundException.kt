package com.jammking.loopbox.domain.exception.task

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class MusicGenerationTaskNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.TASK_NOT_FOUND,
    message = message
) {
    companion object {

        fun byTaskId(taskId: MusicGenerationTaskId) =
            MusicGenerationTaskNotFoundException(
                "Not found: Music generation task not found: taskId=${taskId.value}"
            )

        fun byMusicId(musicId: MusicId) =
            MusicGenerationTaskNotFoundException(
                "Not found: Music generation task not found by music: musicId=${musicId.value}"
            )

        fun byProviderAndExternalId(provider: MusicAiProvider, externalId: ExternalId) =
            MusicGenerationTaskNotFoundException(
                "Not found: Music generation task not found by provider and externalId: provider=${provider.name}, externalId=${externalId.value}"
            )
    }
}