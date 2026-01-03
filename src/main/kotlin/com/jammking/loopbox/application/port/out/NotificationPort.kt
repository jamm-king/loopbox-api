package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId

interface NotificationPort {

    fun notifyVersionGenerationCompleted(
        projectId: ProjectId,
        musicId: MusicId,
        versionIds: List<MusicVersionId>
    )

    fun notifyVersionGenerationFailed(
        projectId: ProjectId,
        musicId: MusicId
    )
}