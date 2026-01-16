package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ImageGenerationTaskJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.ImageGenerationTaskJpaRepository
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JpaImageGenerationTaskRepository(
    private val repository: ImageGenerationTaskJpaRepository
) : ImageGenerationTaskRepository {
    override fun save(task: ImageGenerationTask): ImageGenerationTask {
        val saved = repository.save(ImageGenerationTaskJpaEntity.fromDomain(task))
        return saved.toDomain()
    }

    override fun findById(id: ImageGenerationTaskId): ImageGenerationTask? {
        return repository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun findByImageId(imageId: ImageId): List<ImageGenerationTask> {
        return repository.findByImageId(imageId.value).map { it.toDomain() }
    }

    override fun findByProviderAndExternalId(
        provider: ImageAiProvider,
        externalId: ExternalId
    ): ImageGenerationTask? {
        return repository.findByProviderAndExternalId(provider, externalId.value)?.toDomain()
    }

    override fun findByStatusAndProviderAndUpdatedBefore(
        status: ImageGenerationTaskStatus,
        provider: ImageAiProvider,
        before: Instant
    ): List<ImageGenerationTask> {
        return repository.findByStatusAndProviderAndUpdatedAtBefore(status, provider, before).map { it.toDomain() }
    }

    override fun deleteByImageId(imageId: ImageId) {
        repository.deleteByImageId(imageId.value)
    }

    override fun deleteByStatusBefore(status: ImageGenerationTaskStatus, before: Instant): Int {
        return repository.deleteByStatusAndUpdatedAtBefore(status, before).toInt()
    }
}
