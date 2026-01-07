package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider

interface ImageManagementUseCase {

    fun createImage(projectId: ProjectId): Image
    fun deleteImage(imageId: ImageId)
    fun generateVersion(command: GenerateVersionCommand): Image
    fun deleteVersion(imageId: ImageId, versionId: ImageVersionId): Image
    fun acknowledgeFailure(imageId: ImageId): Image

    data class GenerateVersionCommand(
        val imageId: ImageId,
        val config: ImageConfig,
        val provider: ImageAiProvider
    )
}
