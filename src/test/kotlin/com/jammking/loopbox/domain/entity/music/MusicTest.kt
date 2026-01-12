package com.jammking.loopbox.domain.entity.music

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.music.InvalidMusicStateException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class MusicTest {

    private val projectId = ProjectId(UUID.randomUUID().toString())

    @Test
    fun `startVersionGeneration should update status to GENERATING`() {
        // Given
        val music = Music(projectId = projectId, status = MusicStatus.IDLE)
        val config = MusicConfig(mood = "Happy")
        val now = Instant.now().plusSeconds(10)

        // When
        music.startVersionGeneration(config, now)

        // Then
        assertEquals(MusicStatus.GENERATING, music.status)
        assertEquals(config, music.requestedConfig)
        assertEquals(now, music.updatedAt)
    }

    @Test
    fun `startVersionGeneration should throw exception if not IDLE`() {
        // Given
        val music = Music(projectId = projectId, status = MusicStatus.GENERATING)
        val config = MusicConfig(mood = "Happy")

        // When & Then
        assertThrows(InvalidMusicStateException::class.java) {
            music.startVersionGeneration(config)
        }
    }

    @Test
    fun `completeVersionGeneration should update status to IDLE`() {
        // Given
        val music = Music(projectId = projectId, status = MusicStatus.GENERATING)
        val now = Instant.now().plusSeconds(10)

        // When
        music.completeVersionGeneration(now)

        // Then
        assertEquals(MusicStatus.IDLE, music.status)
        assertEquals(now, music.updatedAt)
    }

    @Test
    fun `failVersionGeneration should update status to FAILED`() {
        // Given
        val music = Music(projectId = projectId, status = MusicStatus.GENERATING)
        val now = Instant.now().plusSeconds(10)

        // When
        music.failVersionGeneration(now)

        // Then
        assertEquals(MusicStatus.FAILED, music.status)
        assertEquals(MusicOperation.GENERATE_VERSION, music.lastOperation)
        assertEquals(now, music.updatedAt)
    }

    @Test
    fun `acknowledgeFailure should reset status to IDLE`() {
        // Given
        val music = Music(projectId = projectId, status = MusicStatus.FAILED, lastOperation = MusicOperation.GENERATE_VERSION)
        val now = Instant.now().plusSeconds(10)

        // When
        music.acknowledgeFailure(now)

        // Then
        assertEquals(MusicStatus.IDLE, music.status)
        assertNull(music.lastOperation)
        assertEquals(now, music.updatedAt)
    }

    @Test
    fun `copy should keep alias when not overridden`() {
        // Given
        val music = Music(projectId = projectId, alias = "Focus Track")

        // When
        val copied = music.copy()

        // Then
        assertEquals("Focus Track", copied.alias)
    }

    @Test
    fun `updateAlias should update alias and updatedAt`() {
        // Given
        val music = Music(projectId = projectId, alias = "Old Name")
        val now = Instant.now().plusSeconds(5)

        // When
        music.updateAlias("New Name", now)

        // Then
        assertEquals("New Name", music.alias)
        assertEquals(now, music.updatedAt)
    }
}
