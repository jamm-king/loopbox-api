package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant

class InMemoryMusicGenerationTaskRepositoryTest {

    private val repository = InMemoryMusicGenerationTaskRepository()

    @Test
    fun `save should store and return task`() {
        val task = MusicGenerationTask(
            id = MusicGenerationTaskId("task-1"),
            musicId = MusicId("music-1"),
            externalId = ExternalId("external-1"),
            provider = MusicAiProvider.SUNO
        )

        val saved = repository.save(task)
        val found = repository.findById(MusicGenerationTaskId("task-1"))

        assertEquals("task-1", saved.id.value)
        assertEquals("music-1", saved.musicId.value)
        assertEquals("task-1", found?.id?.value)
        assertEquals("music-1", found?.musicId?.value)
    }

    @Test
    fun `findByMusicId should filter by music`() {
        val musicA = MusicId("music-a")
        val musicB = MusicId("music-b")
        repository.save(
            MusicGenerationTask(
                id = MusicGenerationTaskId("task-1"),
                musicId = musicA,
                externalId = ExternalId("ext-1"),
                provider = MusicAiProvider.SUNO
            )
        )
        repository.save(
            MusicGenerationTask(
                id = MusicGenerationTaskId("task-2"),
                musicId = musicA,
                externalId = ExternalId("ext-2"),
                provider = MusicAiProvider.SUNO
            )
        )
        repository.save(
            MusicGenerationTask(
                id = MusicGenerationTaskId("task-3"),
                musicId = musicB,
                externalId = ExternalId("ext-3"),
                provider = MusicAiProvider.SUNO
            )
        )

        val result = repository.findByMusicId(musicA).map { it.id.value }.sorted()

        assertEquals(listOf("task-1", "task-2"), result)
    }

    @Test
    fun `findByProviderAndExternalId should return matching task`() {
        val target = MusicGenerationTask(
            id = MusicGenerationTaskId("task-1"),
            musicId = MusicId("music-1"),
            externalId = ExternalId("external-1"),
            provider = MusicAiProvider.SUNO
        )
        repository.save(target)

        val found = repository.findByProviderAndExternalId(
            provider = MusicAiProvider.SUNO,
            externalId = ExternalId("external-1")
        )

        assertEquals("task-1", found?.id?.value)
    }

    @Test
    fun `deleteByMusicId should remove tasks for music`() {
        val targetMusic = MusicId("music-1")
        repository.save(
            MusicGenerationTask(
                id = MusicGenerationTaskId("task-1"),
                musicId = targetMusic,
                externalId = ExternalId("ext-1"),
                provider = MusicAiProvider.SUNO
            )
        )
        repository.save(
            MusicGenerationTask(
                id = MusicGenerationTaskId("task-2"),
                musicId = targetMusic,
                externalId = ExternalId("ext-2"),
                provider = MusicAiProvider.SUNO
            )
        )
        repository.save(
            MusicGenerationTask(
                id = MusicGenerationTaskId("task-3"),
                musicId = MusicId("music-2"),
                externalId = ExternalId("ext-3"),
                provider = MusicAiProvider.SUNO
            )
        )

        repository.deleteByMusicId(targetMusic)

        assertNull(repository.findById(MusicGenerationTaskId("task-1")))
        assertNull(repository.findById(MusicGenerationTaskId("task-2")))
        assertEquals("task-3", repository.findById(MusicGenerationTaskId("task-3"))?.id?.value)
    }

    @Test
    fun `deleteByStatusBefore should remove old canceled tasks`() {
        val cutoff = Instant.parse("2026-01-01T00:00:00Z")
        val oldTask = MusicGenerationTask(
            id = MusicGenerationTaskId("task-1"),
            musicId = MusicId("music-1"),
            externalId = ExternalId("ext-1"),
            status = MusicGenerationTaskStatus.CANCELED,
            provider = MusicAiProvider.SUNO,
            updatedAt = Instant.parse("2025-12-01T00:00:00Z")
        )
        val recentTask = MusicGenerationTask(
            id = MusicGenerationTaskId("task-2"),
            musicId = MusicId("music-1"),
            externalId = ExternalId("ext-2"),
            status = MusicGenerationTaskStatus.CANCELED,
            provider = MusicAiProvider.SUNO,
            updatedAt = Instant.parse("2026-01-02T00:00:00Z")
        )
        val activeTask = MusicGenerationTask(
            id = MusicGenerationTaskId("task-3"),
            musicId = MusicId("music-1"),
            externalId = ExternalId("ext-3"),
            status = MusicGenerationTaskStatus.GENERATING,
            provider = MusicAiProvider.SUNO,
            updatedAt = Instant.parse("2025-12-01T00:00:00Z")
        )

        repository.save(oldTask)
        repository.save(recentTask)
        repository.save(activeTask)

        val deleted = repository.deleteByStatusBefore(MusicGenerationTaskStatus.CANCELED, cutoff)

        assertEquals(1, deleted)
        assertNull(repository.findById(MusicGenerationTaskId("task-1")))
        assertEquals("task-2", repository.findById(MusicGenerationTaskId("task-2"))?.id?.value)
        assertEquals("task-3", repository.findById(MusicGenerationTaskId("task-3"))?.id?.value)
    }
}
