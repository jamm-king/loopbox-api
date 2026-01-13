package com.jammking.loopbox.domain.exception.video

import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.ValidationException

class InvalidVideoEditException(
    override val message: String
): ValidationException(
    errorCode = ErrorCode.INVALID_VIDEO_EDIT,
    message = message
)
