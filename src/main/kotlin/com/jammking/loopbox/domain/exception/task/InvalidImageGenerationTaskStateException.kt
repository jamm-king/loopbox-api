package com.jammking.loopbox.domain.exception.task

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class InvalidImageGenerationTaskStateException(
    val taskId: ImageGenerationTaskId,
    val imageId: ImageId,
    val currentStatus: ImageGenerationTaskStatus,
    val attemptedAction: String
): StateViolationException(
    errorCode = ErrorCode.TASK_NOT_FOUND,
    message = "State violation: Cannot $attemptedAction when image generation task is $currentStatus: taskId=${taskId.value}, imageId=${imageId.value}"
)
