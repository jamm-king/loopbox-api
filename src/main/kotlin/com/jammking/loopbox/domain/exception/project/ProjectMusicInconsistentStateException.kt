package com.jammking.loopbox.domain.exception.project

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.InconsistentStateException

class ProjectMusicInconsistentStateException(
    override val message: String
): InconsistentStateException(
    errorCode = ErrorCode.INCONSISTENT_PROJECT_MUSIC_RELATION,
    message = message
) {
    companion object {
        fun projectMissingForMusic(musicId: MusicId, projectId: ProjectId) =
            ProjectMusicInconsistentStateException(
                "Inconsistent state: Project not found for existing music: musicId=${musicId.value}, projectId=${projectId.value}"
            )
    }
}