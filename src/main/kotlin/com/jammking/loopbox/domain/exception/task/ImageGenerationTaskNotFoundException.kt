package com.jammking.loopbox.domain.exception.task

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class ImageGenerationTaskNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.TASK_NOT_FOUND,
    message = message
) {
    companion object {

        fun byTaskId(taskId: ImageGenerationTaskId) =
            ImageGenerationTaskNotFoundException(
                "Not found: Image generation task not found: taskId=${taskId.value}"
            )

        fun byImageId(imageId: ImageId) =
            ImageGenerationTaskNotFoundException(
                "Not found: Image generation task not found by image: imageId=${imageId.value}"
            )

        fun byProviderAndExternalId(provider: ImageAiProvider, externalId: ExternalId) =
            ImageGenerationTaskNotFoundException(
                "Not found: Image generation task not found by provider and externalId: provider=${provider.name}, externalId=${externalId.value}"
            )
    }
}
