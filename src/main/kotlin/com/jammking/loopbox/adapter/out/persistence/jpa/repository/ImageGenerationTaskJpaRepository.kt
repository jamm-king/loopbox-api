package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ImageGenerationTaskJpaEntity
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface ImageGenerationTaskJpaRepository : JpaRepository<ImageGenerationTaskJpaEntity, String> {
    fun findByImageId(imageId: String): List<ImageGenerationTaskJpaEntity>
    fun findByProviderAndExternalId(provider: ImageAiProvider, externalId: String): ImageGenerationTaskJpaEntity?
    fun findByStatusAndProviderAndUpdatedAtBefore(
        status: ImageGenerationTaskStatus,
        provider: ImageAiProvider,
        updatedAt: Instant
    ): List<ImageGenerationTaskJpaEntity>
    fun deleteByImageId(imageId: String): Long
    fun deleteByStatusAndUpdatedAtBefore(status: ImageGenerationTaskStatus, updatedAt: Instant): Long
}
