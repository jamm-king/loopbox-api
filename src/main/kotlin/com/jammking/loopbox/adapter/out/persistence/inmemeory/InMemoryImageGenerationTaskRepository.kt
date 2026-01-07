package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryImageGenerationTaskRepository: ImageGenerationTaskRepository {

    private val store = ConcurrentHashMap<String, ImageGenerationTask>()

    override fun save(task: ImageGenerationTask): ImageGenerationTask {
        val stored = task.copy()
        store[task.id.value] = stored
        return stored.copy()
    }

    override fun findById(id: ImageGenerationTaskId): ImageGenerationTask? =
        store[id.value]

    override fun findByImageId(imageId: ImageId): List<ImageGenerationTask> =
        store.values.filter { it.imageId == imageId }

    override fun findByProviderAndExternalId(provider: ImageAiProvider, externalId: ExternalId): ImageGenerationTask? =
        store.values.firstOrNull { it.provider == provider && it.externalId == externalId }

    override fun deleteByImageId(imageId: ImageId) {
        val targetIds = store.values.filter { it.imageId == imageId }.map { it.id }
        targetIds.forEach { store.remove(it.value) }
    }

    override fun deleteByStatusBefore(status: ImageGenerationTaskStatus, before: Instant): Int {
        val targetIds = store.values
            .filter { it.status == status && it.updatedAt.isBefore(before) }
            .map { it.id }
        targetIds.forEach { store.remove(it.value) }
        return targetIds.size
    }
}
