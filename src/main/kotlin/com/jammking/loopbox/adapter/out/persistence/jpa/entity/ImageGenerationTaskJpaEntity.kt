package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "image_generation_tasks")
class ImageGenerationTaskJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "image_id", nullable = false, length = 64)
    var imageId: String = "",
    @Column(name = "external_id", nullable = false, length = 128)
    var externalId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: ImageGenerationTaskStatus = ImageGenerationTaskStatus.REQUESTED,
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 64)
    var provider: ImageAiProvider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
    @Column(name = "poll_count", nullable = false)
    var pollCount: Int = 0,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH,
    @Column(name = "error_message", columnDefinition = "text")
    var errorMessage: String? = null
) {
    fun toDomain(): ImageGenerationTask = ImageGenerationTask(
        id = ImageGenerationTaskId(id),
        imageId = ImageId(imageId),
        externalId = ExternalId(externalId),
        status = status,
        provider = provider,
        pollCount = pollCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        errorMessage = errorMessage
    )

    companion object {
        fun fromDomain(task: ImageGenerationTask): ImageGenerationTaskJpaEntity = ImageGenerationTaskJpaEntity(
            id = task.id.value,
            imageId = task.imageId.value,
            externalId = task.externalId.value,
            status = task.status,
            provider = task.provider,
            pollCount = task.pollCount,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            errorMessage = task.errorMessage
        )
    }
}
