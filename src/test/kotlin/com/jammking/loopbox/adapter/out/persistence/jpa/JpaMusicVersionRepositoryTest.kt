package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("postgresql")
@Import(JpaMusicVersionRepository::class)
class JpaMusicVersionRepositoryTest {

    @Autowired
    private lateinit var repository: JpaMusicVersionRepository

    @Test
    fun `save should store and return music version`() {
        val version = MusicVersion(
            id = MusicVersionId("version-1"),
            musicId = MusicId("music-1"),
            config = MusicConfig(mood = "calm"),
            durationSeconds = 120
        )

        val saved = repository.save(version)
        val found = repository.findById(MusicVersionId("version-1"))

        assertEquals(saved.id.value, found?.id?.value)
        assertEquals(120, found?.durationSeconds)
        assertEquals("calm", found?.config?.mood)
    }

    @Test
    fun `findByMusicId should return versions and deleteByMusicId should remove`() {
        repository.save(
            MusicVersion(
                id = MusicVersionId("version-1"),
                musicId = MusicId("music-1"),
                config = MusicConfig(mood = "a")
            )
        )
        repository.save(
            MusicVersion(
                id = MusicVersionId("version-2"),
                musicId = MusicId("music-1"),
                config = MusicConfig(mood = "b")
            )
        )

        val result = repository.findByMusicId(MusicId("music-1")).map { it.id.value }.sorted()
        assertEquals(listOf("version-1", "version-2"), result)

        repository.deleteByMusicId(MusicId("music-1"))
        assertNull(repository.findById(MusicVersionId("version-1")))
        assertNull(repository.findById(MusicVersionId("version-2")))
    }
}
