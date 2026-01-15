package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.video.Video

interface VideoManagementUseCase {

    fun updateVideo(command: UpdateVideoCommand): Video
    fun requestRender(userId: UserId, projectId: ProjectId): Video

    data class UpdateVideoCommand(
        val userId: UserId,
        val projectId: ProjectId,
        val segments: List<SegmentInput>,
        val imageGroups: List<ImageGroupInput>
    )

    data class SegmentInput(
        val musicVersionId: MusicVersionId
    )

    data class ImageGroupInput(
        val imageVersionId: ImageVersionId,
        val segmentIndexStart: Int,
        val segmentIndexEnd: Int
    )
}
