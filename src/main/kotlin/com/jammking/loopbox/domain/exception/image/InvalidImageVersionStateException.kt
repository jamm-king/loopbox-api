package com.jammking.loopbox.domain.exception.image

import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.image.ImageVersionStatus
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class InvalidImageVersionStateException(
    val versionId: ImageVersionId,
    val currentStatus: ImageVersionStatus,
    val attemptedAction: String
): StateViolationException(
    errorCode = ErrorCode.INVALID_IMAGE_VERSION_STATE,
    message = "State violation: Cannot $attemptedAction when image version is $currentStatus: versionId=${versionId.value}"
)
