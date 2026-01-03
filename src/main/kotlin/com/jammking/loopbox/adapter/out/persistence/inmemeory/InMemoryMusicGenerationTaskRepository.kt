package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.exception.task.MusicGenerationTaskNotFoundException
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryMusicGenerationTaskRepository: MusicGenerationTaskRepository {

    private val store = ConcurrentHashMap<String, MusicGenerationTask>()

    override fun save(task: MusicGenerationTask): MusicGenerationTask {
        val stored = task.copy()
        store[task.id.value] = stored
        return stored.copy()
    }

    override fun findById(id: MusicGenerationTaskId): MusicGenerationTask? =
        store[id.value]

    override fun findByMusicId(musicId: MusicId): List<MusicGenerationTask> =
        store.values.filter { it.musicId == musicId }

    override fun findByProviderAndExternalId(provider: MusicAiProvider, externalId: ExternalId): MusicGenerationTask? =
        store.values.firstOrNull { it.provider == provider && it.externalId == externalId }

    override fun deleteByMusicId(musicId: MusicId) {
        val targetIds = store.values.filter { it.musicId == musicId }.map { it.id }
        targetIds.forEach { store.remove(it.value) }
    }
}