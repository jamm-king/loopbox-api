package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import java.time.Instant

interface ImageGenerationTaskRepository {
    fun save(task: ImageGenerationTask): ImageGenerationTask
    fun findById(id: ImageGenerationTaskId): ImageGenerationTask?
    fun findByImageId(imageId: ImageId): List<ImageGenerationTask>
    fun findByProviderAndExternalId(provider: ImageAiProvider, externalId: ExternalId): ImageGenerationTask?
    fun deleteByImageId(imageId: ImageId)
    fun deleteByStatusBefore(status: ImageGenerationTaskStatus, before: Instant): Int
}
