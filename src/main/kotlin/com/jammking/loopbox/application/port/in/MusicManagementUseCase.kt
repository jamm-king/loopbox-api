package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.music.*
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider

interface MusicManagementUseCase {

    fun createMusic(projectId: ProjectId, alias: String? = null): Music
    fun updateMusic(command: UpdateMusicCommand): Music
    fun deleteMusic(musicId: MusicId)
    fun generateVersion(command: GenerateVersionCommand): Music
    fun deleteVersion(musicId: MusicId, versionId: MusicVersionId): Music
    fun acknowledgeFailure(musicId: MusicId): Music

    data class UpdateMusicCommand(
        val musicId: MusicId,
        val alias: String?
    )

    data class GenerateVersionCommand(
        val musicId: MusicId,
        val config: MusicConfig,
        val provider: MusicAiProvider
    )
}
