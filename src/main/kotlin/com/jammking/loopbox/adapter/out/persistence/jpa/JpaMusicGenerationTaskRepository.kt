package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.MusicGenerationTaskJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.MusicGenerationTaskJpaRepository
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JpaMusicGenerationTaskRepository(
    private val repository: MusicGenerationTaskJpaRepository
) : MusicGenerationTaskRepository {
    override fun save(task: MusicGenerationTask): MusicGenerationTask {
        val saved = repository.save(MusicGenerationTaskJpaEntity.fromDomain(task))
        return saved.toDomain()
    }

    override fun findById(id: MusicGenerationTaskId): MusicGenerationTask? {
        return repository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun findByMusicId(musicId: MusicId): List<MusicGenerationTask> {
        return repository.findByMusicId(musicId.value).map { it.toDomain() }
    }

    override fun findByProviderAndExternalId(
        provider: MusicAiProvider,
        externalId: ExternalId
    ): MusicGenerationTask? {
        return repository.findByProviderAndExternalId(provider, externalId.value)?.toDomain()
    }

    override fun deleteByMusicId(musicId: MusicId) {
        repository.deleteByMusicId(musicId.value)
    }

    override fun deleteByStatusBefore(status: MusicGenerationTaskStatus, before: Instant): Int {
        return repository.deleteByStatusAndUpdatedAtBefore(status, before).toInt()
    }
}
