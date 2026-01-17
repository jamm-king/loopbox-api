package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.domain.entity.file.VideoFile
import com.jammking.loopbox.domain.entity.file.VideoFileId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "video_files")
class VideoFileJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "path", nullable = false, columnDefinition = "text")
    var path: String = ""
) {
    fun toDomain(): VideoFile = VideoFile(
        id = VideoFileId(id),
        path = path
    )

    companion object {
        fun fromDomain(file: VideoFile): VideoFileJpaEntity = VideoFileJpaEntity(
            id = file.id.value,
            path = file.path
        )
    }
}
