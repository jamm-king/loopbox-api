package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.Video

interface VideoRepository {
    fun save(video: Video): Video
    fun findByProjectId(projectId: ProjectId): Video?
    fun deleteByProjectId(projectId: ProjectId)
}
