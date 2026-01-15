package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.user.UserId
import java.nio.file.Path

interface GetMusicVersionAudioUseCase {

    fun getAudioTarget(userId: UserId, musicId: MusicId, versionId: MusicVersionId): AudioStreamTarget

    data class AudioStreamTarget(
        val path: Path,
        val contentType: String,
        val contentLength: Long
    )
}
