package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "music_generation_tasks")
class MusicGenerationTaskJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "music_id", nullable = false, length = 64)
    var musicId: String = "",
    @Column(name = "external_id", nullable = false, length = 128)
    var externalId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: MusicGenerationTaskStatus = MusicGenerationTaskStatus.REQUESTED,
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 64)
    var provider: MusicAiProvider = MusicAiProvider.SUNO,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH,
    @Column(name = "error_message", columnDefinition = "text")
    var errorMessage: String? = null
) {
    fun toDomain(): MusicGenerationTask = MusicGenerationTask(
        id = MusicGenerationTaskId(id),
        musicId = MusicId(musicId),
        externalId = ExternalId(externalId),
        status = status,
        provider = provider,
        createdAt = createdAt,
        updatedAt = updatedAt,
        errorMessage = errorMessage
    )

    companion object {
        fun fromDomain(task: MusicGenerationTask): MusicGenerationTaskJpaEntity = MusicGenerationTaskJpaEntity(
            id = task.id.value,
            musicId = task.musicId.value,
            externalId = task.externalId.value,
            status = task.status,
            provider = task.provider,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            errorMessage = task.errorMessage
        )
    }
}
