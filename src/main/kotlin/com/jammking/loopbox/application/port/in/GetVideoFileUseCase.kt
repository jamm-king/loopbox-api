package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import java.nio.file.Path

interface GetVideoFileUseCase {

    fun getVideoTarget(userId: UserId, projectId: ProjectId): VideoStreamTarget

    data class VideoStreamTarget(
        val path: Path,
        val contentType: String,
        val contentLength: Long
    )
}
