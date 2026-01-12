package com.jammking.loopbox.domain.entity.music

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.music.InvalidMusicStateException
import java.time.Instant
import java.util.*

class Music(
    val id: MusicId = MusicId(UUID.randomUUID().toString()),
    val projectId: ProjectId,
    val alias: String? = null,
    status: MusicStatus = MusicStatus.IDLE,
    requestedConfig: MusicConfig? = null,
    lastOperation: MusicOperation? = null,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) {

    var status: MusicStatus = status
        private set

    var requestedConfig: MusicConfig? = requestedConfig
        private set

    var lastOperation: MusicOperation? = lastOperation
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun startVersionGeneration(musicConfig: MusicConfig, now: Instant = Instant.now()) {
        if(!isIdle()) throw InvalidMusicStateException(this, "start version generation")

        this.requestedConfig = musicConfig
        this.status = MusicStatus.GENERATING
        this.updatedAt = now
    }

    fun completeVersionGeneration(now: Instant = Instant.now()) {
        if(!isGenerating()) throw InvalidMusicStateException(this, "complete version generation")

        this.status = MusicStatus.IDLE
        this.updatedAt = now
    }

    fun failVersionGeneration(now: Instant = Instant.now()) {
        if(!isGenerating()) throw InvalidMusicStateException(this, "fail version generation")

        this.status = MusicStatus.FAILED
        this.lastOperation = MusicOperation.GENERATE_VERSION
        this.updatedAt = now
    }

    fun startVersionDeletion(now: Instant = Instant.now()) {
        if(!isIdle()) throw InvalidMusicStateException(this, "start version deletion")

        this.status = MusicStatus.DELETING
        this.updatedAt = now
    }

    fun completeVersionDeletion(now: Instant = Instant.now()) {
        if(!isDeleting()) throw InvalidMusicStateException(this, "complete version deletion")

        this.status = MusicStatus.IDLE
        this.updatedAt = now
    }

    fun failVersionDeletion(now: Instant = Instant.now()) {
        if(!isDeleting()) throw InvalidMusicStateException(this, "fail version deletion")

        this.status = MusicStatus.FAILED
        this.lastOperation = MusicOperation.DELETE_VERSION
        this.updatedAt = now
    }

    fun acknowledgeFailure(now: Instant = Instant.now()) {
        if(!isFailed()) throw InvalidMusicStateException(this, "acknowledge failure")

        this.status = MusicStatus.IDLE
        this.lastOperation = null
        this.updatedAt = now
    }

    fun copy(
        id: MusicId = this.id,
        projectId: ProjectId = this.projectId,
        alias: String? = this.alias,
        status: MusicStatus = this.status,
        requestedConfig: MusicConfig? = this.requestedConfig,
        lastOperation: MusicOperation? = this.lastOperation,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt
    ): Music = Music(
        id = id,
        projectId = projectId,
        alias = alias,
        status = status,
        requestedConfig = requestedConfig,
        lastOperation = lastOperation,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun isIdle() = status == MusicStatus.IDLE
    fun isGenerating() = status == MusicStatus.GENERATING
    fun isDeleting() = status == MusicStatus.DELETING
    fun isFailed() = status == MusicStatus.FAILED
}
