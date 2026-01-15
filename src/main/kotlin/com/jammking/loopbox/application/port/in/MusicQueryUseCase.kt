package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId

interface MusicQueryUseCase {

    fun getMusicDetail(userId: UserId, musicId: MusicId): GetMusicDetailResult
    fun getMusicListForProject(userId: UserId, projectId: ProjectId): List<Music>

    data class GetMusicDetailResult(
        val music: Music,
        val versions: List<MusicVersion>
    )
}
