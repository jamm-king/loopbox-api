package com.jammking.loopbox.domain.entity.image

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.image.InvalidImageStateException
import java.time.Instant
import java.util.UUID

class Image(
    val id: ImageId = ImageId(UUID.randomUUID().toString()),
    val projectId: ProjectId,
    status: ImageStatus = ImageStatus.IDLE,
    requestedConfig: ImageConfig? = null,
    lastOperation: ImageOperation? = null,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) {

    var status: ImageStatus = status
        private set

    var requestedConfig: ImageConfig? = requestedConfig
        private set

    var lastOperation: ImageOperation? = lastOperation
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun startVersionGeneration(imageConfig: ImageConfig, now: Instant = Instant.now()) {
        if(!isIdle()) throw InvalidImageStateException(this, "start version generation")

        this.requestedConfig = imageConfig
        this.status = ImageStatus.GENERATING
        this.updatedAt = now
    }

    fun completeVersionGeneration(now: Instant = Instant.now()) {
        if(!isGenerating()) throw InvalidImageStateException(this, "complete version generation")

        this.status = ImageStatus.IDLE
        this.updatedAt = now
    }

    fun failVersionGeneration(now: Instant = Instant.now()) {
        if(!isGenerating()) throw InvalidImageStateException(this, "fail version generation")

        this.status = ImageStatus.FAILED
        this.lastOperation = ImageOperation.GENERATE_VERSION
        this.updatedAt = now
    }

    fun startVersionDeletion(now: Instant = Instant.now()) {
        if(!isIdle()) throw InvalidImageStateException(this, "start version deletion")

        this.status = ImageStatus.DELETING
        this.updatedAt = now
    }

    fun completeVersionDeletion(now: Instant = Instant.now()) {
        if(!isDeleting()) throw InvalidImageStateException(this, "complete version deletion")

        this.status = ImageStatus.IDLE
        this.updatedAt = now
    }

    fun failVersionDeletion(now: Instant = Instant.now()) {
        if(!isDeleting()) throw InvalidImageStateException(this, "fail version deletion")

        this.status = ImageStatus.FAILED
        this.lastOperation = ImageOperation.DELETE_VERSION
        this.updatedAt = now
    }

    fun acknowledgeFailure(now: Instant = Instant.now()) {
        if(!isFailed()) throw InvalidImageStateException(this, "acknowledge failure")

        this.status = ImageStatus.IDLE
        this.lastOperation = null
        this.updatedAt = now
    }

    fun copy(
        id: ImageId = this.id,
        projectId: ProjectId = this.projectId,
        status: ImageStatus = this.status,
        requestedConfig: ImageConfig? = this.requestedConfig,
        lastOperation: ImageOperation? = this.lastOperation,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt
    ): Image = Image(
        id = id,
        projectId = projectId,
        status = status,
        requestedConfig = requestedConfig,
        lastOperation = lastOperation,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun isIdle() = status == ImageStatus.IDLE
    fun isGenerating() = status == ImageStatus.GENERATING
    fun isDeleting() = status == ImageStatus.DELETING
    fun isFailed() = status == ImageStatus.FAILED
}
