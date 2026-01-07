package com.jammking.loopbox.domain.entity.image

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.image.InvalidImageStateException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ImageTest {

    private val projectId = ProjectId(UUID.randomUUID().toString())

    @Test
    fun `startVersionGeneration should update status to GENERATING`() {
        // Given
        val image = Image(projectId = projectId, status = ImageStatus.IDLE)
        val config = ImageConfig(description = "A sunrise")
        val now = Instant.now().plusSeconds(10)

        // When
        image.startVersionGeneration(config, now)

        // Then
        assertEquals(ImageStatus.GENERATING, image.status)
        assertEquals(config, image.requestedConfig)
        assertEquals(now, image.updatedAt)
    }

    @Test
    fun `startVersionGeneration should throw exception if not IDLE`() {
        // Given
        val image = Image(projectId = projectId, status = ImageStatus.GENERATING)
        val config = ImageConfig(description = "A sunrise")

        // When & Then
        assertThrows(InvalidImageStateException::class.java) {
            image.startVersionGeneration(config)
        }
    }

    @Test
    fun `completeVersionGeneration should update status to IDLE`() {
        // Given
        val image = Image(projectId = projectId, status = ImageStatus.GENERATING)
        val now = Instant.now().plusSeconds(10)

        // When
        image.completeVersionGeneration(now)

        // Then
        assertEquals(ImageStatus.IDLE, image.status)
        assertEquals(now, image.updatedAt)
    }

    @Test
    fun `failVersionGeneration should update status to FAILED`() {
        // Given
        val image = Image(projectId = projectId, status = ImageStatus.GENERATING)
        val now = Instant.now().plusSeconds(10)

        // When
        image.failVersionGeneration(now)

        // Then
        assertEquals(ImageStatus.FAILED, image.status)
        assertEquals(ImageOperation.GENERATE_VERSION, image.lastOperation)
        assertEquals(now, image.updatedAt)
    }

    @Test
    fun `acknowledgeFailure should reset status to IDLE`() {
        // Given
        val image = Image(
            projectId = projectId,
            status = ImageStatus.FAILED,
            lastOperation = ImageOperation.GENERATE_VERSION
        )
        val now = Instant.now().plusSeconds(10)

        // When
        image.acknowledgeFailure(now)

        // Then
        assertEquals(ImageStatus.IDLE, image.status)
        assertNull(image.lastOperation)
        assertEquals(now, image.updatedAt)
    }
}
