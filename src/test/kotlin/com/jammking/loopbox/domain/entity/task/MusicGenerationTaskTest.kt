package com.jammking.loopbox.domain.entity.task

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.exception.task.InvalidMusicGenerationTaskStateException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class MusicGenerationTaskTest {

    private val musicId = MusicId("music-1")
    private val externalId = ExternalId("ext-1")

    @Test
    fun `markGenerating should update status to GENERATING`() {
        // Given
        val task = MusicGenerationTask(
            musicId = musicId,
            externalId = externalId,
            provider = MusicAiProvider.SUNO
        )
        val now = Instant.now().plusSeconds(10)

        // When
        task.markGenerating(now)

        // Then
        assertEquals(MusicGenerationTaskStatus.GENERATING, task.status)
        assertEquals(now, task.updatedAt)
    }

    @Test
    fun `markGenerating should throw exception if not REQUESTED`() {
        // Given
        val task = MusicGenerationTask(
            musicId = musicId,
            externalId = externalId,
            provider = MusicAiProvider.SUNO,
            status = MusicGenerationTaskStatus.GENERATING
        )

        // When & Then
        assertThrows(InvalidMusicGenerationTaskStateException::class.java) {
            task.markGenerating()
        }
    }

    @Test
    fun `markCompleted should update status to COMPLETED`() {
        // Given
        val task = MusicGenerationTask(
            musicId = musicId,
            externalId = externalId,
            provider = MusicAiProvider.SUNO,
            status = MusicGenerationTaskStatus.GENERATING
        )
        val now = Instant.now().plusSeconds(10)

        // When
        task.markCompleted(now)

        // Then
        assertEquals(MusicGenerationTaskStatus.COMPLETED, task.status)
        assertEquals(now, task.updatedAt)
    }

    @Test
    fun `markFailed should update status to FAILED`() {
        // Given
        val task = MusicGenerationTask(
            musicId = musicId,
            externalId = externalId,
            provider = MusicAiProvider.SUNO,
            status = MusicGenerationTaskStatus.GENERATING
        )
        val now = Instant.now().plusSeconds(10)
        val errorMessage = "Generation failed"

        // When
        task.markFailed(errorMessage, now)

        // Then
        assertEquals(MusicGenerationTaskStatus.FAILED, task.status)
        assertEquals(errorMessage, task.errorMessage)
        assertEquals(now, task.updatedAt)
    }
}
