package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class InMemoryMusicRepositoryTest {

    private val repository = InMemoryMusicRepository()

    @Test
    fun `save should store and return music`() {
        val projectId = ProjectId("project-1")
        val music = Music(id = MusicId("music-1"), projectId = projectId)

        val saved = repository.save(music)
        val found = repository.findById(MusicId("music-1"))

        assertEquals("music-1", saved.id.value)
        assertEquals("project-1", saved.projectId.value)
        assertEquals("music-1", found?.id?.value)
        assertEquals("project-1", found?.projectId?.value)
    }

    @Test
    fun `findByProjectId should filter by project`() {
        val projectA = ProjectId("project-a")
        val projectB = ProjectId("project-b")
        repository.save(Music(id = MusicId("music-1"), projectId = projectA))
        repository.save(Music(id = MusicId("music-2"), projectId = projectA))
        repository.save(Music(id = MusicId("music-3"), projectId = projectB))

        val result = repository.findByProjectId(projectA).map { it.id.value }.sorted()

        assertEquals(listOf("music-1", "music-2"), result)
    }

    @Test
    fun `deleteById should remove music`() {
        repository.save(Music(id = MusicId("music-1"), projectId = ProjectId("project-1")))

        repository.deleteById(MusicId("music-1"))

        assertNull(repository.findById(MusicId("music-1")))
    }
}
