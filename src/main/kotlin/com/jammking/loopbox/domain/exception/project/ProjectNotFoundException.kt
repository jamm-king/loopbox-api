package com.jammking.loopbox.domain.exception.project

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException
import org.springframework.web.client.HttpClientErrorException.NotFound

class ProjectNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.PROJECT_NOT_FOUND,
    message = message
) {
    companion object {

        fun byProjectId(projectId: ProjectId) =
            ProjectNotFoundException(
                "Not found: Project not found: projectId=${projectId.value}"
            )

        fun byMusicId(musicId: MusicId) =
            ProjectNotFoundException(
                "Not Found: Project not found by music: musicId=${musicId.value}"
            )
    }
}