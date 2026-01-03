package com.jammking.loopbox.domain.exception.file

import com.jammking.loopbox.domain.entity.file.AudioFileId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class AudioFileNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.RESOURCE_NOT_FOUND,
    message = message
) {
    companion object {
        fun byAudioFileId(audioFileId: AudioFileId) =
            AudioFileNotFoundException(
                message = "Not found: Audio file not found: audioFileId=${audioFileId.value}"
            )
    }
}