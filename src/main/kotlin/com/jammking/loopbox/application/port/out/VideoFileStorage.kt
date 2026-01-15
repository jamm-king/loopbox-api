package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.VideoId

interface VideoFileStorage {
    fun prepareRenderPath(projectId: ProjectId, videoId: VideoId): String
}
