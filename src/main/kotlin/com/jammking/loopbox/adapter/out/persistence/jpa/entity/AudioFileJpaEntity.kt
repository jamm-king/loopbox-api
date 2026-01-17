package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.file.AudioFileId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "audio_files")
class AudioFileJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "path", nullable = false, columnDefinition = "text")
    var path: String = ""
) {
    fun toDomain(): AudioFile = AudioFile(
        id = AudioFileId(id),
        path = path
    )

    companion object {
        fun fromDomain(file: AudioFile): AudioFileJpaEntity = AudioFileJpaEntity(
            id = file.id.value,
            path = file.path
        )
    }
}
