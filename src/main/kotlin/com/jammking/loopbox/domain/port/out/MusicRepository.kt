package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.ProjectId

interface MusicRepository {
    fun save(music: Music): Music
    fun findById(id: MusicId): Music?
    fun findByProjectId(projectId: ProjectId): List<Music>
    fun deleteById(id: MusicId)
}