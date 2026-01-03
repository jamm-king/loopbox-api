package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId

interface MusicVersionRepository {
    fun save(version: MusicVersion): MusicVersion
    fun findById(versionId: MusicVersionId): MusicVersion?
    fun findByMusicId(musicId: MusicId): List<MusicVersion>
    fun deleteById(versionId: MusicVersionId)
    fun deleteByMusicId(musicId: MusicId)
}