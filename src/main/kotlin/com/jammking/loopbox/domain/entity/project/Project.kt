package com.jammking.loopbox.domain.entity.project

import com.jammking.loopbox.domain.exception.project.InvalidProjectStateException
import com.jammking.loopbox.domain.exception.project.InvalidProjectTitleException
import com.jammking.loopbox.domain.entity.user.UserId
import java.time.Instant
import java.util.*

class Project(
    val id: ProjectId = ProjectId(UUID.randomUUID().toString()),
    val ownerUserId: UserId,
    title: String,
    status: ProjectStatus = ProjectStatus.DRAFT,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) {

    init {
        if(!validateTitle(title)) throw InvalidProjectTitleException(id, title)
    }

    var title: String = title
        private set

    var status: ProjectStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun rename(newTitle: String, now: Instant = Instant.now()) {
        if(!validateTitle(newTitle)) throw InvalidProjectTitleException(id, newTitle)

        title = newTitle
        updatedAt = now
    }

    fun markDraft(now: Instant = Instant.now()) {
        if(!isMusicReady()) throw InvalidProjectStateException(id, status, "mark draft")

        status = ProjectStatus.DRAFT
        updatedAt = now
    }

    fun markMusicReady(now: Instant = Instant.now()) {
        status = ProjectStatus.MUSIC_READY
        updatedAt = now
    }

    fun copy(
        id: ProjectId = this.id,
        ownerUserId: UserId = this.ownerUserId,
        title: String = this.title,
        status: ProjectStatus = this.status,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt
    ): Project = Project(
        id = id,
        ownerUserId = ownerUserId,
        title = title,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun isDraft() = status == ProjectStatus.DRAFT
    fun isMusicReady() = status == ProjectStatus.MUSIC_READY

    private fun validateTitle(title: String): Boolean {
        if(title.isBlank()) return false
        return true
    }
}
