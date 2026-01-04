package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class InMemoryMusicVersionRepositoryTest {

    private val repository = InMemoryMusicVersionRepository()

    @Test
    fun `save should store and return music version`() {
        val version = MusicVersion(
            id = MusicVersionId("version-1"),
            musicId = MusicId("music-1"),
            config = MusicConfig()
        )

        val saved = repository.save(version)
        val found = repository.findById(MusicVersionId("version-1"))

        assertEquals("version-1", saved.id.value)
        assertEquals("music-1", saved.musicId.value)
        assertEquals("version-1", found?.id?.value)
        assertEquals("music-1", found?.musicId?.value)
    }

    @Test
    fun `findByMusicId should filter by music`() {
        val musicA = MusicId("music-a")
        val musicB = MusicId("music-b")
        repository.save(MusicVersion(id = MusicVersionId("version-1"), musicId = musicA, config = MusicConfig()))
        repository.save(MusicVersion(id = MusicVersionId("version-2"), musicId = musicA, config = MusicConfig()))
        repository.save(MusicVersion(id = MusicVersionId("version-3"), musicId = musicB, config = MusicConfig()))

        val result = repository.findByMusicId(musicA).map { it.id.value }.sorted()

        assertEquals(listOf("version-1", "version-2"), result)
    }

    @Test
    fun `deleteById should remove version`() {
        repository.save(MusicVersion(id = MusicVersionId("version-1"), musicId = MusicId("music-1"), config = MusicConfig()))

        repository.deleteById(MusicVersionId("version-1"))

        assertNull(repository.findById(MusicVersionId("version-1")))
    }

    @Test
    fun `deleteByMusicId should remove versions for music`() {
        val targetMusic = MusicId("music-1")
        repository.save(MusicVersion(id = MusicVersionId("version-1"), musicId = targetMusic, config = MusicConfig()))
        repository.save(MusicVersion(id = MusicVersionId("version-2"), musicId = targetMusic, config = MusicConfig()))
        repository.save(MusicVersion(id = MusicVersionId("version-3"), musicId = MusicId("music-2"), config = MusicConfig()))

        repository.deleteByMusicId(targetMusic)

        assertNull(repository.findById(MusicVersionId("version-1")))
        assertNull(repository.findById(MusicVersionId("version-2")))
        assertEquals("version-3", repository.findById(MusicVersionId("version-3"))?.id?.value)
    }
}
