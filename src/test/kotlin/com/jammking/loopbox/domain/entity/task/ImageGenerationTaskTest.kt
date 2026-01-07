package com.jammking.loopbox.domain.entity.task

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.exception.task.InvalidImageGenerationTaskStateException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ImageGenerationTaskTest {

    @Test
    fun `markGenerating should update status to GENERATING`() {
        val task = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4
        )

        task.markGenerating()

        assertEquals(ImageGenerationTaskStatus.GENERATING, task.status)
    }

    @Test
    fun `markCompleted should update status to COMPLETED`() {
        val task = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            status = ImageGenerationTaskStatus.GENERATING
        )

        task.markCompleted()

        assertEquals(ImageGenerationTaskStatus.COMPLETED, task.status)
    }

    @Test
    fun `markCompleted should throw exception if not GENERATING`() {
        val task = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            status = ImageGenerationTaskStatus.REQUESTED
        )

        assertThrows(InvalidImageGenerationTaskStateException::class.java) {
            task.markCompleted()
        }
    }

    @Test
    fun `markFailed should update status to FAILED`() {
        val task = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            status = ImageGenerationTaskStatus.GENERATING
        )

        task.markFailed("error")

        assertEquals(ImageGenerationTaskStatus.FAILED, task.status)
    }

    @Test
    fun `markCanceled should update status to CANCELED`() {
        val task = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            status = ImageGenerationTaskStatus.GENERATING
        )

        task.markCanceled()

        assertEquals(ImageGenerationTaskStatus.CANCELED, task.status)
    }
}
