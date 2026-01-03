package com.jammking.loopbox.domain.exception.music

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class MusicNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.MUSIC_NOT_FOUND,
    message = message
) {
    companion object {

        fun byMusicId(musicId: MusicId) =
            MusicNotFoundException(
                "Not found: Music not found: musicId=${musicId.value}"
            )

        fun byProjectId(projectId: ProjectId) =
            MusicNotFoundException(
                "Not found: Music not found: projectId=${projectId.value}"
            )

        fun byTaskId(taskId: MusicGenerationTaskId) =
            MusicNotFoundException(
                "Not found: Music not found: taskId=${taskId.value}"
            )
    }
}