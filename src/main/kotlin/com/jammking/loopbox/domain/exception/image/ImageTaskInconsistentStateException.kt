package com.jammking.loopbox.domain.exception.image

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.InconsistentStateException

class ImageTaskInconsistentStateException(
    override val message: String
): InconsistentStateException(
    errorCode = ErrorCode.INCONSISTENT_IMAGE_TASK_RELATION,
    message = message
) {
    companion object {
        fun imageMissingForTask(taskId: ImageGenerationTaskId, imageId: ImageId) =
            ImageTaskInconsistentStateException(
                "Inconsistent state: Image not found for existing task: taskId=${taskId.value}, imageId=${imageId.value}"
            )
    }
}
