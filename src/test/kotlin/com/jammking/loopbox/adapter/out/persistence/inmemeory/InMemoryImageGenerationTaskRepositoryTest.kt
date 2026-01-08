package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class InMemoryImageGenerationTaskRepositoryTest {

    private val repository = InMemoryImageGenerationTaskRepository()

    @Test
    fun `findByStatusAndProviderAndUpdatedBefore should return matching tasks`() {
        val provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4
        val cutoff = Instant.now().minusSeconds(60)

        val matchTask = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = provider,
            updatedAt = cutoff.minusSeconds(10)
        )
        val newTask = ImageGenerationTask(
            imageId = ImageId("image-2"),
            externalId = ExternalId("external-2"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = provider,
            updatedAt = cutoff.plusSeconds(10)
        )
        val otherStatusTask = ImageGenerationTask(
            imageId = ImageId("image-3"),
            externalId = ExternalId("external-3"),
            status = ImageGenerationTaskStatus.COMPLETED,
            provider = provider,
            updatedAt = cutoff.minusSeconds(10)
        )
        repository.save(matchTask)
        repository.save(newTask)
        repository.save(otherStatusTask)

        val results = repository.findByStatusAndProviderAndUpdatedBefore(
            status = ImageGenerationTaskStatus.GENERATING,
            provider = provider,
            before = cutoff
        )

        assertEquals(listOf(matchTask.id.value), results.map { it.id.value })
    }
}
