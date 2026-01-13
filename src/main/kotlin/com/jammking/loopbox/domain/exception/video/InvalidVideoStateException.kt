package com.jammking.loopbox.domain.exception.video

import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class InvalidVideoStateException(
    val video: Video,
    val action: String
): StateViolationException(
    errorCode = ErrorCode.INVALID_VIDEO_STATE,
    message = "Invalid video state: action=$action, status=${video.status}"
)
