package com.jammking.loopbox.domain.exception.video

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.VideoId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class VideoNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.VIDEO_NOT_FOUND,
    message = message
) {
    companion object {
        fun byVideoId(videoId: VideoId) =
            VideoNotFoundException(
                "Not found: Video not found: videoId=${videoId.value}"
            )

        fun byProjectId(projectId: ProjectId) =
            VideoNotFoundException(
                "Not found: Video not found: projectId=${projectId.value}"
            )
    }
}
