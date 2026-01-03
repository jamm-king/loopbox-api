package com.jammking.loopbox.domain.exception.music

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class MusicVersionNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.VERSION_NOT_FOUND,
    message = message
) {
    companion object {

        fun byMusicId(musicId: MusicId) =
            MusicVersionNotFoundException(
                "Not found: MusicVersion not found for music: musicId=${musicId.value}"
            )

        fun byVersionId(versionId: MusicVersionId) =
            MusicVersionNotFoundException(
                "Not found: MusicVersion not found: versionId=${versionId.value}"
            )
    }
}