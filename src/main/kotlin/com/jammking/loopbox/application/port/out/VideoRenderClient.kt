package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.VideoId

interface VideoRenderClient {
    fun requestRender(command: RenderCommand)

    data class RenderCommand(
        val projectId: ProjectId,
        val videoId: VideoId,
        val outputPath: String,
        val segments: List<RenderSegment>,
        val imageGroups: List<RenderImageGroup>
    )

    data class RenderSegment(
        val musicVersionId: String,
        val audioPath: String,
        val durationSeconds: Int
    )

    data class RenderImageGroup(
        val imageVersionId: String,
        val imagePath: String,
        val segmentIndexStart: Int,
        val segmentIndexEnd: Int
    )
}
