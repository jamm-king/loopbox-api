package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.project.ProjectId

interface MusicQueryUseCase {

    fun getMusicDetail(musicId: MusicId): GetMusicDetailResult
    fun getMusicListForProject(projectId: ProjectId): List<Music>

    data class GetMusicDetailResult(
        val music: Music,
        val versions: List<MusicVersion>
    )
}