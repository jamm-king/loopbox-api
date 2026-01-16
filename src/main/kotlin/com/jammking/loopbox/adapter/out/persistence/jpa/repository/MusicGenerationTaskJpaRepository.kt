package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.MusicGenerationTaskJpaEntity
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface MusicGenerationTaskJpaRepository : JpaRepository<MusicGenerationTaskJpaEntity, String> {
    fun findByMusicId(musicId: String): List<MusicGenerationTaskJpaEntity>
    fun findByProviderAndExternalId(provider: MusicAiProvider, externalId: String): MusicGenerationTaskJpaEntity?
    fun deleteByMusicId(musicId: String): Long
    fun deleteByStatusAndUpdatedAtBefore(status: MusicGenerationTaskStatus, updatedAt: Instant): Long
}
