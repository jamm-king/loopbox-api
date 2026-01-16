package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.Instant

@DataJpaTest
@Import(JpaImageGenerationTaskRepository::class)
class JpaImageGenerationTaskRepositoryTest {

    @Autowired
    private lateinit var repository: JpaImageGenerationTaskRepository

    @Test
    fun `findByProviderAndExternalId should return task`() {
        val task = ImageGenerationTask(
            id = ImageGenerationTaskId("task-1"),
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4
        )
        repository.save(task)

        val found = repository.findByProviderAndExternalId(
            ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            ExternalId("external-1")
        )

        assertEquals("task-1", found?.id?.value)
    }

    @Test
    fun `findByStatusAndProviderAndUpdatedBefore should filter by time`() {
        val oldTask = ImageGenerationTask(
            id = ImageGenerationTaskId("task-old"),
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-old"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        )
        val newTask = ImageGenerationTask(
            id = ImageGenerationTaskId("task-new"),
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-new"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            updatedAt = Instant.parse("2024-02-01T00:00:00Z")
        )
        repository.save(oldTask)
        repository.save(newTask)

        val result = repository.findByStatusAndProviderAndUpdatedBefore(
            ImageGenerationTaskStatus.GENERATING,
            ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            Instant.parse("2024-01-15T00:00:00Z")
        ).map { it.id.value }

        assertEquals(listOf("task-old"), result)
    }

    @Test
    fun `deleteByStatusBefore should remove tasks`() {
        val oldTask = ImageGenerationTask(
            id = ImageGenerationTaskId("task-old"),
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-old"),
            status = ImageGenerationTaskStatus.CANCELED,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        )
        val newTask = ImageGenerationTask(
            id = ImageGenerationTaskId("task-new"),
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-new"),
            status = ImageGenerationTaskStatus.CANCELED,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            updatedAt = Instant.parse("2024-02-01T00:00:00Z")
        )
        repository.save(oldTask)
        repository.save(newTask)

        val deleted = repository.deleteByStatusBefore(
            ImageGenerationTaskStatus.CANCELED,
            Instant.parse("2024-01-15T00:00:00Z")
        )

        assertEquals(1, deleted)
        assertNull(repository.findById(ImageGenerationTaskId("task-old")))
        assertEquals("task-new", repository.findById(ImageGenerationTaskId("task-new"))?.id?.value)
    }
}
