package com.jammking.loopbox.domain.entity.task

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.exception.task.InvalidImageGenerationTaskStateException
import java.time.Instant
import java.util.UUID

class ImageGenerationTask(
    val id: ImageGenerationTaskId = ImageGenerationTaskId(UUID.randomUUID().toString()),
    val imageId: ImageId,
    val externalId: ExternalId,
    status: ImageGenerationTaskStatus = ImageGenerationTaskStatus.REQUESTED,
    val provider: ImageAiProvider,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
    errorMessage: String? = null
) {

    var status: ImageGenerationTaskStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    var errorMessage: String? = errorMessage
        private set

    fun markGenerating(now: Instant = Instant.now()) {
        if(!isRequested()) throw InvalidImageGenerationTaskStateException(id, imageId, status, "generate")

        this.status = ImageGenerationTaskStatus.GENERATING
        this.updatedAt = now
    }

    fun markCompleted(now: Instant = Instant.now()) {
        if(!isGenerating()) throw InvalidImageGenerationTaskStateException(id, imageId, status, "complete")

        this.status = ImageGenerationTaskStatus.COMPLETED
        this.updatedAt = now
    }

    fun markFailed(message: String? = null, now: Instant = Instant.now()) {
        if(!isGenerating()) throw InvalidImageGenerationTaskStateException(id, imageId, status, "fail")

        this.status = ImageGenerationTaskStatus.FAILED
        this.errorMessage = message
        this.updatedAt = now
    }

    fun markCanceled(now: Instant = Instant.now()) {
        this.status = ImageGenerationTaskStatus.CANCELED
        this.updatedAt = now
    }

    fun copy(
        id: ImageGenerationTaskId = this.id,
        imageId: ImageId = this.imageId,
        externalTaskId: ExternalId = this.externalId,
        status: ImageGenerationTaskStatus = this.status,
        provider: ImageAiProvider = this.provider,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
        errorMessage: String? = this.errorMessage
    ): ImageGenerationTask = ImageGenerationTask(
        id = id,
        imageId = imageId,
        status = status,
        provider = provider,
        externalId = externalTaskId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        errorMessage = errorMessage
    )

    fun isRequested() = status == ImageGenerationTaskStatus.REQUESTED
    fun isGenerating() = status == ImageGenerationTaskStatus.GENERATING
    fun isCompleted() = status == ImageGenerationTaskStatus.COMPLETED
    fun isFailed() = status == ImageGenerationTaskStatus.FAILED
    fun isCanceled() = status == ImageGenerationTaskStatus.CANCELED
}
