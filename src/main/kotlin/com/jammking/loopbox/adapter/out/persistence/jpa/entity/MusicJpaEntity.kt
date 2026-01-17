package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.adapter.out.persistence.jpa.converter.MusicConfigConverter
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicOperation
import com.jammking.loopbox.domain.entity.music.MusicStatus
import com.jammking.loopbox.domain.entity.project.ProjectId
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "musics")
class MusicJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "project_id", nullable = false, length = 64)
    var projectId: String = "",
    @Column(name = "alias", length = 255)
    var alias: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: MusicStatus = MusicStatus.IDLE,
    @Convert(converter = MusicConfigConverter::class)
    @Column(name = "requested_config", columnDefinition = "text")
    var requestedConfig: MusicConfig? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "last_operation", length = 32)
    var lastOperation: MusicOperation? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
) {
    fun toDomain(): Music = Music(
        id = MusicId(id),
        projectId = ProjectId(projectId),
        alias = alias,
        status = status,
        requestedConfig = requestedConfig,
        lastOperation = lastOperation,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(music: Music): MusicJpaEntity = MusicJpaEntity(
            id = music.id.value,
            projectId = music.projectId.value,
            alias = music.alias,
            status = music.status,
            requestedConfig = music.requestedConfig,
            lastOperation = music.lastOperation,
            createdAt = music.createdAt,
            updatedAt = music.updatedAt
        )
    }
}
