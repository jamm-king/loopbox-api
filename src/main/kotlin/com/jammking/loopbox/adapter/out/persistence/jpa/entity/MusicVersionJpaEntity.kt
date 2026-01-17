package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.adapter.out.persistence.jpa.converter.MusicConfigConverter
import com.jammking.loopbox.domain.entity.file.AudioFileId
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.music.MusicVersionStatus
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "music_versions")
class MusicVersionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "music_id", nullable = false, length = 64)
    var musicId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: MusicVersionStatus = MusicVersionStatus.GENERATED,
    @Convert(converter = MusicConfigConverter::class)
    @Column(name = "config", nullable = false, columnDefinition = "text")
    var config: MusicConfig? = null,
    @Column(name = "file_id", length = 64)
    var fileId: String? = null,
    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int = 0,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
) {
    fun toDomain(): MusicVersion = MusicVersion(
        id = MusicVersionId(id),
        musicId = MusicId(musicId),
        status = status,
        config = requireNotNull(config),
        fileId = fileId?.let { AudioFileId(it) },
        durationSeconds = durationSeconds,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(version: MusicVersion): MusicVersionJpaEntity = MusicVersionJpaEntity(
            id = version.id.value,
            musicId = version.musicId.value,
            status = version.status,
            config = version.config,
            fileId = version.fileId?.value,
            durationSeconds = version.durationSeconds,
            createdAt = version.createdAt,
            updatedAt = version.updatedAt
        )
    }
}
