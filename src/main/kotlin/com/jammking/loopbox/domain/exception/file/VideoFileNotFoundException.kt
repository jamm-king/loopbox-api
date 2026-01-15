package com.jammking.loopbox.domain.exception.file

import com.jammking.loopbox.domain.entity.file.VideoFileId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class VideoFileNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.RESOURCE_NOT_FOUND,
    message = message
) {
    companion object {
        fun byVideoFileId(videoFileId: VideoFileId) =
            VideoFileNotFoundException(
                message = "Not found: Video file not found: videoFileId=${videoFileId.value}"
            )
    }
}
