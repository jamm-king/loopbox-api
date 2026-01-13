package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.Video

interface VideoQueryUseCase {
    fun getVideoDetail(projectId: ProjectId): Video
}
