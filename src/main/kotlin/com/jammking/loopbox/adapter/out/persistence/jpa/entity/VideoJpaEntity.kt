package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.adapter.out.persistence.jpa.converter.VideoImageGroupsConverter
import com.jammking.loopbox.adapter.out.persistence.jpa.converter.VideoSegmentsConverter
import com.jammking.loopbox.domain.entity.file.VideoFileId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.entity.video.VideoId
import com.jammking.loopbox.domain.entity.video.VideoImageGroup
import com.jammking.loopbox.domain.entity.video.VideoSegment
import com.jammking.loopbox.domain.entity.video.VideoStatus
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "videos")
class VideoJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "project_id", nullable = false, length = 64)
    var projectId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: VideoStatus = VideoStatus.DRAFT,
    @Column(name = "file_id", length = 64)
    var fileId: String? = null,
    @Convert(converter = VideoSegmentsConverter::class)
    @Column(name = "segments_json", nullable = false, columnDefinition = "text")
    var segments: List<VideoSegment> = emptyList(),
    @Convert(converter = VideoImageGroupsConverter::class)
    @Column(name = "image_groups_json", nullable = false, columnDefinition = "text")
    var imageGroups: List<VideoImageGroup> = emptyList(),
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
) {
    fun toDomain(): Video = Video(
        id = VideoId(id),
        projectId = ProjectId(projectId),
        segments = segments,
        imageGroups = imageGroups,
        status = status,
        fileId = fileId?.let { VideoFileId(it) },
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(video: Video): VideoJpaEntity = VideoJpaEntity(
            id = video.id.value,
            projectId = video.projectId.value,
            status = video.status,
            fileId = video.fileId?.value,
            segments = video.segments,
            imageGroups = video.imageGroups,
            createdAt = video.createdAt,
            updatedAt = video.updatedAt
        )
    }
}
