package com.jammking.loopbox.domain.exception.image

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class ImageNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.IMAGE_NOT_FOUND,
    message = message
) {
    companion object {

        fun byImageId(imageId: ImageId) =
            ImageNotFoundException(
                "Not found: Image not found: imageId=${imageId.value}"
            )

        fun byProjectId(projectId: ProjectId) =
            ImageNotFoundException(
                "Not found: Image not found: projectId=${projectId.value}"
            )

        fun byTaskId(taskId: ImageGenerationTaskId) =
            ImageNotFoundException(
                "Not found: Image not found: taskId=${taskId.value}"
            )
    }
}
