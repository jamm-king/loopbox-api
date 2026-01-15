package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.user.UserId

interface ImageManagementUseCase {

    fun createImage(userId: UserId, projectId: ProjectId): Image
    fun deleteImage(userId: UserId, imageId: ImageId)
    fun generateVersion(command: GenerateVersionCommand): Image
    fun deleteVersion(userId: UserId, imageId: ImageId, versionId: ImageVersionId): Image
    fun acknowledgeFailure(userId: UserId, imageId: ImageId): Image

    data class GenerateVersionCommand(
        val userId: UserId,
        val imageId: ImageId,
        val config: ImageConfig,
        val provider: ImageAiProvider
    )
}
