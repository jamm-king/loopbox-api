package com.jammking.loopbox.domain.exception.project

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.InconsistentStateException

class ProjectImageInconsistentStateException(
    override val message: String
): InconsistentStateException(
    errorCode = ErrorCode.INCONSISTENT_PROJECT_IMAGE_RELATION,
    message = message
) {
    companion object {
        fun projectMissingForImage(imageId: ImageId, projectId: ProjectId) =
            ProjectImageInconsistentStateException(
                "Inconsistent state: Project not found for existing image: imageId=${imageId.value}, projectId=${projectId.value}"
            )
    }
}
