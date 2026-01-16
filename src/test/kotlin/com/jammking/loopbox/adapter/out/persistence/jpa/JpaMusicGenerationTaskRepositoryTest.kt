package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.Instant

@DataJpaTest
@Import(JpaMusicGenerationTaskRepository::class)
class JpaMusicGenerationTaskRepositoryTest {

    @Autowired
    private lateinit var repository: JpaMusicGenerationTaskRepository

    @Test
    fun `findByProviderAndExternalId should return task`() {
        val task = MusicGenerationTask(
            id = MusicGenerationTaskId("task-1"),
            musicId = MusicId("music-1"),
            externalId = ExternalId("external-1"),
            status = MusicGenerationTaskStatus.GENERATING,
            provider = MusicAiProvider.SUNO
        )
        repository.save(task)

        val found = repository.findByProviderAndExternalId(MusicAiProvider.SUNO, ExternalId("external-1"))

        assertEquals("task-1", found?.id?.value)
    }

    @Test
    fun `deleteByStatusBefore should remove tasks`() {
        val oldTask = MusicGenerationTask(
            id = MusicGenerationTaskId("task-old"),
            musicId = MusicId("music-1"),
            externalId = ExternalId("external-old"),
            status = MusicGenerationTaskStatus.CANCELED,
            provider = MusicAiProvider.SUNO,
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        )
        val newTask = MusicGenerationTask(
            id = MusicGenerationTaskId("task-new"),
            musicId = MusicId("music-1"),
            externalId = ExternalId("external-new"),
            status = MusicGenerationTaskStatus.CANCELED,
            provider = MusicAiProvider.SUNO,
            updatedAt = Instant.parse("2024-02-01T00:00:00Z")
        )
        repository.save(oldTask)
        repository.save(newTask)

        val deleted = repository.deleteByStatusBefore(
            MusicGenerationTaskStatus.CANCELED,
            Instant.parse("2024-01-15T00:00:00Z")
        )

        assertEquals(1, deleted)
        assertNull(repository.findById(MusicGenerationTaskId("task-old")))
        assertEquals("task-new", repository.findById(MusicGenerationTaskId("task-new"))?.id?.value)
    }
}
