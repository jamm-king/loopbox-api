package com.jammking.loopbox.domain.exception.image

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class InvalidImageStateException(
    val image: Image,
    val attemptedAction: String
): StateViolationException(
    errorCode = ErrorCode.INVALID_IMAGE_STATE,
    message = "State violation: Cannot $attemptedAction: imageId=${image.id.value}, status=${image.status.name}, lastOperation=${image.lastOperation?.name}, requestedConfig=${image.requestedConfig}"
)
