package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.project.ProjectId
import java.nio.file.Path

interface GetVideoFileUseCase {

    fun getVideoTarget(projectId: ProjectId): VideoStreamTarget

    data class VideoStreamTarget(
        val path: Path,
        val contentType: String,
        val contentLength: Long
    )
}
