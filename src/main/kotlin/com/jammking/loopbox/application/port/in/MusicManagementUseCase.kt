package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.music.*
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.user.UserId

interface MusicManagementUseCase {

    fun createMusic(userId: UserId, projectId: ProjectId, alias: String? = null): Music
    fun updateMusic(command: UpdateMusicCommand): Music
    fun deleteMusic(userId: UserId, musicId: MusicId)
    fun generateVersion(command: GenerateVersionCommand): Music
    fun deleteVersion(userId: UserId, musicId: MusicId, versionId: MusicVersionId): Music
    fun acknowledgeFailure(userId: UserId, musicId: MusicId): Music

    data class UpdateMusicCommand(
        val userId: UserId,
        val musicId: MusicId,
        val alias: String?
    )

    data class GenerateVersionCommand(
        val userId: UserId,
        val musicId: MusicId,
        val config: MusicConfig,
        val provider: MusicAiProvider
    )
}
