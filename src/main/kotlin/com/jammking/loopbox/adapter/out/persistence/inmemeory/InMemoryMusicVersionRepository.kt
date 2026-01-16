package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("inmemory")
class InMemoryMusicVersionRepository: MusicVersionRepository {

    private val store = ConcurrentHashMap<String, MusicVersion>()

    override fun save(version: MusicVersion): MusicVersion {
        val stored = version.copy()
        store[version.id.value] = stored
        return stored.copy()
    }

    override fun findById(versionId: MusicVersionId): MusicVersion? =
        store[versionId.value]

    override fun findByMusicId(musicId: MusicId): List<MusicVersion> =
        store.values.filter { it.musicId == musicId }

    override fun deleteById(versionId: MusicVersionId) {
        store.remove(versionId.value)
    }

    override fun deleteByMusicId(musicId: MusicId) {
        val targetIds = store.values.filter { it.musicId == musicId }.map { it.id }
        targetIds.forEach { store.remove(it.value) }
    }
}
