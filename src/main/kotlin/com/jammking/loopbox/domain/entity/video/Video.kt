package com.jammking.loopbox.domain.entity.video

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.file.VideoFileId
import com.jammking.loopbox.domain.exception.video.InvalidVideoStateException
import java.time.Instant
import java.util.UUID

class Video(
    val id: VideoId = VideoId(UUID.randomUUID().toString()),
    val projectId: ProjectId,
    segments: List<VideoSegment> = emptyList(),
    imageGroups: List<VideoImageGroup> = emptyList(),
    status: VideoStatus = VideoStatus.DRAFT,
    fileId: VideoFileId? = null,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) {

    var segments: List<VideoSegment> = segments
        private set

    var imageGroups: List<VideoImageGroup> = imageGroups
        private set

    var status: VideoStatus = status
        private set

    var fileId: VideoFileId? = fileId
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun updateTimeline(
        segments: List<VideoSegment>,
        imageGroups: List<VideoImageGroup>,
        now: Instant = Instant.now()
    ) {
        this.segments = segments
        this.imageGroups = imageGroups
        this.status = VideoStatus.DRAFT
        this.fileId = null
        this.updatedAt = now
    }

    fun startRender(now: Instant = Instant.now()) {
        if (status == VideoStatus.RENDERING) {
            throw InvalidVideoStateException(this, "start render")
        }

        this.status = VideoStatus.RENDERING
        this.fileId = null
        this.updatedAt = now
    }

    fun completeRender(fileId: VideoFileId, now: Instant = Instant.now()) {
        if (status != VideoStatus.RENDERING) {
            throw InvalidVideoStateException(this, "complete render")
        }

        this.status = VideoStatus.READY
        this.fileId = fileId
        this.updatedAt = now
    }

    fun failRender(now: Instant = Instant.now()) {
        if (status != VideoStatus.RENDERING) {
            throw InvalidVideoStateException(this, "fail render")
        }

        this.status = VideoStatus.FAILED
        this.updatedAt = now
    }

    fun totalDurationSeconds(): Int =
        segments.sumOf { it.durationSeconds }

    fun copy(
        id: VideoId = this.id,
        projectId: ProjectId = this.projectId,
        segments: List<VideoSegment> = this.segments,
        imageGroups: List<VideoImageGroup> = this.imageGroups,
        status: VideoStatus = this.status,
        fileId: VideoFileId? = this.fileId,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt
    ): Video = Video(
        id = id,
        projectId = projectId,
        segments = segments.map { it.copy() },
        imageGroups = imageGroups.map { it.copy() },
        status = status,
        fileId = fileId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
