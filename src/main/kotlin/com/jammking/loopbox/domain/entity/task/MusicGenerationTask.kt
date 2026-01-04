package com.jammking.loopbox.domain.entity.task

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.exception.task.InvalidMusicGenerationTaskStateException
import java.time.Instant
import java.util.UUID

class MusicGenerationTask(
    val id: MusicGenerationTaskId = MusicGenerationTaskId(UUID.randomUUID().toString()),
    val musicId: MusicId,
    val externalId: ExternalId,
    status: MusicGenerationTaskStatus = MusicGenerationTaskStatus.REQUESTED,
    val provider: MusicAiProvider,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
    errorMessage: String? = null
) {

    var status: MusicGenerationTaskStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    var errorMessage: String? = errorMessage
        private set

    fun markGenerating(now: Instant = Instant.now()) {
        if(!isRequested()) throw InvalidMusicGenerationTaskStateException(id, musicId, status, "generate")

        this.status = MusicGenerationTaskStatus.GENERATING
        this.updatedAt = now
    }

    fun markCompleted(now: Instant = Instant.now()) {
        if(!isGenerating()) throw InvalidMusicGenerationTaskStateException(id, musicId, status, "complete")

        this.status = MusicGenerationTaskStatus.COMPLETED
        this.updatedAt = now
    }

    fun markFailed(message: String? = null, now: Instant = Instant.now()) {
        if(!isGenerating()) throw InvalidMusicGenerationTaskStateException(id, musicId, status, "fail")

        this.status = MusicGenerationTaskStatus.FAILED
        this.errorMessage = message
        this.updatedAt = now
    }

    fun markCanceled(now: Instant = Instant.now()) {
        this.status = MusicGenerationTaskStatus.CANCELED
        this.updatedAt = now
    }

    fun copy(
        id: MusicGenerationTaskId = this.id,
        musicId: MusicId = this.musicId,
        externalTaskId: ExternalId = this.externalId,
        status: MusicGenerationTaskStatus = this.status,
        provider: MusicAiProvider = this.provider,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
        errorMessage: String? = this.errorMessage
    ): MusicGenerationTask = MusicGenerationTask(
        id = id,
        musicId = musicId,
        status = status,
        provider = provider,
        externalId = externalTaskId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        errorMessage = errorMessage
    )

    fun isRequested() = status == MusicGenerationTaskStatus.REQUESTED
    fun isGenerating() = status == MusicGenerationTaskStatus.GENERATING
    fun isCompleted() = status == MusicGenerationTaskStatus.COMPLETED
    fun isFailed() = status == MusicGenerationTaskStatus.FAILED
    fun isCanceled() = status == MusicGenerationTaskStatus.CANCELED
}
