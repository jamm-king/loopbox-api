package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryMusicRepository: MusicRepository {

    private val store = ConcurrentHashMap<String, Music>()

    override fun save(music: Music): Music {
        val stored = music.copy()
        store[music.id.value] = stored
        return stored.copy()
    }

    override fun findByProjectId(projectId: ProjectId): List<Music> =
        store.values.filter { it.projectId == projectId }

    override fun findById(id: MusicId): Music? =
        store[id.value]

    override fun deleteById(id: MusicId) {
        store.remove(id.value)
    }
}