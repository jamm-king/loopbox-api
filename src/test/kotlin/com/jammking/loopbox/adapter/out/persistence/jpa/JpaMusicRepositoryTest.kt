package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(JpaMusicRepository::class)
class JpaMusicRepositoryTest {

    @Autowired
    private lateinit var repository: JpaMusicRepository

    @Test
    fun `save should store and return music`() {
        val music = Music(
            id = MusicId("music-1"),
            projectId = ProjectId("project-1"),
            alias = "alias",
            requestedConfig = MusicConfig(mood = "happy")
        )

        val saved = repository.save(music)
        val found = repository.findById(MusicId("music-1"))

        assertEquals(saved.id.value, found?.id?.value)
        assertEquals("alias", found?.alias)
        assertEquals("happy", found?.requestedConfig?.mood)
    }

    @Test
    fun `findByProjectId should return musics`() {
        repository.save(Music(id = MusicId("music-1"), projectId = ProjectId("project-1")))
        repository.save(Music(id = MusicId("music-2"), projectId = ProjectId("project-1")))
        repository.save(Music(id = MusicId("music-3"), projectId = ProjectId("project-2")))

        val result = repository.findByProjectId(ProjectId("project-1")).map { it.id.value }.sorted()

        assertEquals(listOf("music-1", "music-2"), result)
    }
}
