package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId

interface MusicFileStorage {
    fun saveFromRemoteUrl(
        remoteUrl: String,
        projectId: ProjectId,
        musicId: MusicId,
        versionId: MusicVersionId
    ): String
}